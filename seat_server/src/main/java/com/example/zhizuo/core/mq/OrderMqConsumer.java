package com.example.zhizuo.core.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class OrderMqConsumer {

    @Resource
    private OrderService orderService;

    /**
     * 监听死信队列（即超时后的订单消息）
     */
    @RabbitListener(queues = "zhizuo.order.dlx.queue")
    public void onOrderTimeout(Message message, Channel channel) throws IOException {
        String orderNo = new String(message.getBody());
        log.info("收到订单超时消息, 订单号: {}", orderNo);

        try {
            // 查询订单状态
            Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));

            if (order != null) {
                // 如果订单还是 PENDING (未支付) 状态，则执行关闭
                // 注意：如果你的业务逻辑是创建即PAID，这个逻辑可能永远不会触发，除非你把创建时的初始状态改为 PENDING
                if ("PENDING".equals(order.getStatus()) || "PENDING_PAY".equals(order.getStatus())) {
                    orderService.closeOrderAndRestoreStock(order.getId(), "支付超时系统自动关闭");
                } else {
                    log.info("订单 [{}] 状态为 {}, 无需超时处理", orderNo, order.getStatus());
                }
            }

            // 手动确认消息已消费
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("处理超时订单失败", e);
            // 拒绝消息，false表示不再放回队列（或者根据策略放回死信）
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}