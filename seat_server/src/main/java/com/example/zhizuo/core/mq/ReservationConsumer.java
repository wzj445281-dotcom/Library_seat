package com.example.zhizuo.core.mq;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.entity.User;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ReservationConsumer {

    private final ReservationMapper reservationMapper;
    private final UserMapper userMapper;

    public ReservationConsumer(ReservationMapper reservationMapper, UserMapper userMapper) {
        this.reservationMapper = reservationMapper;
        this.userMapper = userMapper;
    }

    /**
     * 监听死信队列 (order.release.queue)
     * 只有当消息过了 15 分钟后，才会出现在这里
     */
    @RabbitListener(queues = "order.release.queue")
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderTimeout(Long reservationId, Message message, Channel channel) throws IOException {
        log.info("收到过期订单检查消息，订单ID: {}", reservationId);

        try {
            // 1. 查订单状态
            Reservation r = reservationMapper.selectById(reservationId);

            // 如果订单不存在，或者已经签到/取消/完成，则直接确认消息，不做处理
            if (r == null || !"RESERVED".equals(r.getStatus())) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            // 2. 再次校验时间 (双重保险)
            // 规则：如果当前时间 > 开始时间 + 15分钟，且状态仍是 RESERVED，则违约
            // 注意：RabbitMQ 只是延时触发，具体的违约判定逻辑要严谨
            LocalDateTime deadline = r.getStartTime().plusMinutes(15);

            if (LocalDateTime.now().isAfter(deadline)) {
                log.warn("订单 {} 超时未签到，执行违约处理", reservationId);

                // 3. 标记违约
                r.setStatus("VIOLATION");
                reservationMapper.updateById(r);

                // 4. 扣信用分
                updateUserCredit(r.getUserId(), -5);
            }

            // 5. 手动确认消息已消费
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("处理死信消息失败", e);
            // 拒绝消息，false表示不再放回队列 (防止死循环)
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private void updateUserCredit(Long userId, int delta) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            int newScore = Math.max(0, Math.min(110, user.getCreditScore() + delta));
            user.setCreditScore(newScore);
            userMapper.updateById(user);
        }
    }
}