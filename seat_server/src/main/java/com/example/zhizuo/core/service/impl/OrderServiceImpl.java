package com.example.zhizuo.core.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.entity.OrderItem;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.mapper.OrderItemMapper;
import com.example.zhizuo.core.mapper.OrderMapper;
import com.example.zhizuo.core.mapper.ProductMapper;
import com.example.zhizuo.core.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ProductMapper productMapper;

    @Override
    public Map<String, Object> getAdminOrderList(String status) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            wrapper.eq(Order::getStatus, status);
        } else {
            // 默认不查未支付的订单，因为商家只关心已付款的
            wrapper.ne(Order::getStatus, "PENDING");
            wrapper.ne(Order::getStatus, "PENDING_PAY");
        }
        wrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = this.list(wrapper);

        // 2. 填充关联数据 (订单项)
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            List<OrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds)
            );

            // 内存分组
            Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));

            for (Order order : orders) {
                order.setProducts(itemsMap.getOrDefault(order.getId(), new ArrayList<>()));
            }
        }

        // 3. 计算统计数据
        Map<String, Object> result = new HashMap<>();
        result.put("list", orders);

        // 简单统计 (真实场景建议用 SQL count 优化性能)
        List<Order> allActive = this.list(new LambdaQueryWrapper<Order>().ne(Order::getStatus, "PENDING_PAY"));
        result.put("pendingCount", allActive.stream().filter(o -> "PENDING_PAY".equals(o.getStatus())).count());
        result.put("paidCount", allActive.stream().filter(o -> "PAID".equals(o.getStatus())).count());
        result.put("readyCount", allActive.stream().filter(o -> "READY".equals(o.getStatus())).count());

        // 计算今日销售额
        BigDecimal todaySales = allActive.stream()
                .filter(o -> o.getPayTime() != null && o.getPayTime().toLocalDate().isEqual(LocalDateTime.now().toLocalDate()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todaySales", todaySales.toString());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, String nextStatus) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        // 简单的状态机校验
        // PAID -> READY -> COMPLETED
        String current = order.getStatus();

        if ("PAID".equals(current) && "READY".equals(nextStatus)) {
            // 配货完成
            order.setStatus("READY");
            // 生成取货码 (如果是自取)
            if (order.getDeliveryType() == 0) {
                order.setPickupCode("C-" + RandomUtil.randomNumbers(3));
            }
        } else if ("READY".equals(current) && "COMPLETED".equals(nextStatus)) {
            // 核销/送达
            order.setStatus("COMPLETED");
        } else {
            // 允许强制流转，但记录日志
            log.warn("强制更改订单状态: {} -> {}", current, nextStatus);
            order.setStatus(nextStatus);
        }

        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType) {
        // 1. 预计算总价 & 扣库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer qty = Integer.valueOf(item.get("quantity").toString());

            Product product = productMapper.selectById(productId);
            if (product == null || product.getStock() < qty) {
                throw new RuntimeException("商品 " + productId + " 库存不足");
            }

            // 扣减库存 (简单的乐观锁或直接扣减)
            product.setStock(product.getStock() - qty);
            product.setSales(product.getSales() + qty);
            productMapper.updateById(product);

            // 组装明细
            OrderItem oi = new OrderItem();
            oi.setProductId(productId);
            oi.setProductName(product.getName());
            oi.setPrice(product.getPrice());
            oi.setQuantity(qty);
            orderItems.add(oi);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        // 2. 保存订单主表
        Order order = new Order();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDeliveryType(deliveryType);
        order.setPayStatus(1); // 模拟直接已支付
        order.setPayTime(LocalDateTime.now());
        order.setStatus("PAID"); // 直接进入制作中

        this.save(order);

        // 3. 保存明细表
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        return order.getOrderNo();
    }
}