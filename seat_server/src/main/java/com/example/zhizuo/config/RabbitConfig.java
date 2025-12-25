package com.example.zhizuo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // 1. 定义交换机
    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange("order-event-exchange", true, false);
    }

    // 2. 定义死信队列 (实际被消费的队列)
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.queue", true, false, false);
    }

    // 3. 定义延时队列 (设置 TTL，消息过期后转发到死信交换机)
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order-event-exchange"); // 过期后转发到哪个交换机
        args.put("x-dead-letter-routing-key", "order.release");     // 转发时的路由键
        args.put("x-message-ttl", 900000);                          // 全局过期时间: 15分钟 (单位ms)

        return new Queue("order.delay.queue", true, false, false, args);
    }

    // 4. 绑定关系

    // 绑定死信队列: order-event-exchange -> order.release -> order.release.queue
    @Bean
    public Binding orderReleaseBinding() {
        return BindingBuilder.bind(orderReleaseQueue())
                .to(orderEventExchange())
                .with("order.release");
    }

    // 绑定延时队列: order-event-exchange -> order.create -> order.delay.queue
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderEventExchange())
                .with("order.create");
    }
}