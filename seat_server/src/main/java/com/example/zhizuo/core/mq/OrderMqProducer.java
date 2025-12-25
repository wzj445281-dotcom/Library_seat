package com.example.zhizuo.core.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class OrderMqProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送订单创建消息 (进入延时队列)
     * @param orderNo 订单号
     */
    public void sendOrderTimeoutMsg(String orderNo) {
        log.info("发送订单超时延时消息, 订单号: {}", orderNo);
        // 路由键 order.create 指向延时队列
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", orderNo);
    }
}