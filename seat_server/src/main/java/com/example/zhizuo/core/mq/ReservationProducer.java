package com.example.zhizuo.core.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class ReservationProducer {

    private final RabbitTemplate rabbitTemplate;

    public ReservationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送延迟消息到 order.delay.queue (预约成功后调用)
     * 
     * 消息流程：
     * 1. 发送到 order-event-exchange 交换机，路由键为 order.create
     * 2. 根据 RabbitConfig 配置，路由键 order.create 会路由到 order.delay.queue 队列
     * 3. 消息在 order.delay.queue 中等待 TTL 过期后，会转发到死信队列进行超时处理
     * 
     * @param reservationId 预约订单ID
     */
    public void sendDelayMessage(Long reservationId) {
        // 通过交换机发送，路由键 order.create 会路由到 order.delay.queue
        // 消息内容：预约订单ID（Long类型）
        log.info("发送预约延迟消息到 order.delay.queue，预约ID: {}", reservationId);
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", reservationId);
    }
}