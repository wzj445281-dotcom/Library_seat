package com.petsaas.core.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationProducer {

    private static final Logger log = LoggerFactory.getLogger(ReservationProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public ReservationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送延迟消�?(预约成功后调�?
     * @param reservationId 预约订单ID
     */
    public void sendDelayMessage(Long reservationId) {
        // 发送到 order-event-exchange 交换机，路由键为 order.create
        // 消息内容就是 订单ID
        log.info("发送预约延迟消息，订单ID: {}", reservationId);
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", reservationId);
    }
}