package com.petsaas.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // 1. 定义交换�?
    @Bean
    public DirectExchange orderEventExchange() {
        // durable=true, autoDelete=false
        return new DirectExchange("order-event-exchange", true, false);
    }

    // 2. 定义死信队列 (实际消费队列) - 用于接收过期消息
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.queue", true);
    }

    // 3. 定义延迟队列 (TTL队列) - 消息发到这里，暂�?5分钟
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 核心配置：消息过期后，转发给 order-event-exchange 交换�?
        args.put("x-dead-letter-exchange", "order-event-exchange");
        // 核心配置：转发时的路由键
        args.put("x-dead-letter-routing-key", "order.release");
        // 核心配置：TTL 过期时间 15分钟 (单位毫秒) = 15 * 60 * 1000
        args.put("x-message-ttl", 900000);

        return QueueBuilder.durable("order.delay.queue").withArguments(args).build();
    }

    // 4. 绑定关系
    // 绑定死信队列
    @Bean
    public Binding orderReleaseBinding() {
        return BindingBuilder.bind(orderReleaseQueue()).to(orderEventExchange()).with("order.release");
    }

    // 绑定延迟队列
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderEventExchange()).with("order.create");
    }
}