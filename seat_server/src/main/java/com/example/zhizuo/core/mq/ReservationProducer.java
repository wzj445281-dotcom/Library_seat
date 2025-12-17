package com.example.zhizuo.core.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservationProducer {

    private final RabbitTemplate rabbitTemplate;

    public ReservationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送延迟消息 (预约成功后调用)
     * @param reservationId 预约订单ID
     */
    public void sendDelayMessage(Long reservationId) {
        // 发送到 order-event-exchange 交换机，路由键为 order.create
        // 消息内容就是 订单ID
        log.info("发送预约延迟消息，订单ID: {}", reservationId);
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", reservationId);
    }
}