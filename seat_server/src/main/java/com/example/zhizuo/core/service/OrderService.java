package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    IPage<Order> pageWithItems(Page<Order> page, Wrapper<Order> queryWrapper);

    Order getDetailWithItems(Long id);

    // 修改此处：返回 List<Order> 而不是 Map
    List<Order> getAdminOrderList(String status);

    void updateOrderStatus(Long id, String status);

    String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType, Long userCouponId, String addressInfo, String remark);

    void closeOrderAndRestoreStock(Long orderId, String reason);

    List<Order> getUserOrderList(Long userId, String status);

    Order getByOrderNo(String orderNo);
}