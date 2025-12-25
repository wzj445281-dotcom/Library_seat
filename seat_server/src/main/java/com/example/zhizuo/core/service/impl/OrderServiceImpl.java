package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.entity.OrderItem;
import com.example.zhizuo.core.mapper.OrderItemMapper;
import com.example.zhizuo.core.mapper.OrderMapper;
import com.example.zhizuo.core.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public IPage<Order> pageWithItems(Page<Order> page, Wrapper<Order> queryWrapper) {
        // 1. 执行主表查询
        IPage<Order> result = this.page(page, queryWrapper);

        // 2. 如果结果为空，直接返回
        if (result.getRecords().isEmpty()) {
            return result;
        }

        // 3. 批量查询关联的商品明细 (避免 N+1 问题)
        // 提取查询到的所有订单 ID
        List<Long> orderIds = result.getRecords().stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        // 一次性查出所有相关商品
        List<OrderItem> allItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().in(OrderItem::getOrderId, orderIds)
        );

        // 4. 在内存中分组：Map<OrderId, List<OrderItem>>
        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 5. 将商品明细填充回对应的订单对象中
        result.getRecords().forEach(order -> {
            order.setProducts(itemMap.getOrDefault(order.getId(), Collections.emptyList()));
        });

        return result;
    }

    @Override
    public Order getDetailWithItems(Long id) {
        // 1. 查询主表
        Order order = this.getById(id);
        if (order == null) {
            return null;
        }

        // 2. 查询子表
        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderId, id)
        );

        // 3. 填充
        order.setProducts(items);

        return order;
    }

    @Override
    public Map<String, Object> getAdminOrderList(String status) {
        // 简单实现，实际项目中可能需要更复杂的逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("count", 0);
        return result;
    }

    @Override
    public void updateOrderStatus(Long id, String status) {
        Order order = this.getById(id);
        if (order != null) {
            order.setStatus(status);
            this.updateById(order);
        }
    }

    @Override
    public String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType) {
        // 简单实现，生成订单号
        String orderNo = "ORDER" + System.currentTimeMillis();
        
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setDeliveryType(deliveryType);
        order.setStatus("PENDING");
        this.save(order);
        
        return orderNo;
    }

    @Override
    public void closeOrderAndRestoreStock(Long orderId, String reason) {
        Order order = this.getById(orderId);
        if (order != null) {
            order.setStatus("CANCELLED");
            this.updateById(order);
        }
    }

    @Override
    public List<Order> getUserOrderList(Long userId, String status) {
        return this.list(Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime));
    }
}