package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    /**
     * 管理端：获取订单列表 (含商品明细)
     * @param status 订单状态 (ALL/PAID/READY/COMPLETED)
     */
    Map<String, Object> getAdminOrderList(String status);

    /**
     * 管理端：更新订单状态
     * @param orderId 订单ID
     * @param nextStatus 下一个状态
     */
    void updateOrderStatus(Long orderId, String nextStatus);

    /**
     * App端：创建订单 (模拟实现，为了闭环)
     */
    String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType);
}