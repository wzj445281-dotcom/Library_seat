package com.example.zhizuo.core.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.service.OrderService;
import com.example.zhizuo.core.service.ProductService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
public class OrderMqConsumer {

    @Resource
    private OrderService orderService;

    // 注意：如果有库存服务应注入库存服务，这里简化为 ProductService
    @Resource
    private ProductService productService;

    /**
     * 监听死信队列 (order.release.queue)
     */
    @RabbitListener(queues = "order.release.queue")
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderTimeout(Message message, Channel channel) throws IOException {
        String orderNo = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        log.info("收到订单超时检查消息: {}", orderNo);

        try {
            Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));

            // 如果订单存在 且 状态为 "PENDING" (待支付)
            if (order != null && "PENDING".equals(order.getStatus())) {
                log.info("订单 {} 超时未支付，执行取消操作", orderNo);

                // 1. 更新订单状态为 CANCELLED
                order.setStatus("CANCELLED");
                orderService.updateById(order);

                // 2. 恢复库存 (简单示例，真实场景需查询 OrderItem 循环恢复)
                // List<OrderItem> items = ...
                // for (OrderItem item : items) { ... productService.addStock(...) }
                log.info("订单 {} 库存已释放(模拟)", orderNo);
            } else {
                log.info("订单 {} 状态为 {}，无需自动取消", orderNo, order == null ? "NULL" : order.getStatus());
            }

            // 手动确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理订单超时消息失败", e);
            // 拒绝消息，不重回队列 (避免死循环)
            channel.basicReject(deliveryTag, false);
        }
    }
}