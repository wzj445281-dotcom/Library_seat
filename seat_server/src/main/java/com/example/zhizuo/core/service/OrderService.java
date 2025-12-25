package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    /**
     * 商家后台：查询订单列表
     */
    Map<String, Object> getAdminOrderList(String status);

    /**
     * APP端：创建订单
     */
    String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType);

    /**
     * 通用：更新订单状态 (状态机流转)
     */
    void updateOrderStatus(Long orderId, String nextStatus);

    /**
     * [新增] 核心业务：关闭订单并恢复库存
     * 场景：用户主动取消、支付超时自动取消
     */
    void closeOrderAndRestoreStock(Long orderId, String reason);

    /**
     * [新增] APP端：获取用户自己的订单列表
     */
    List<Order> getUserOrderList(Long userId, String status);
}