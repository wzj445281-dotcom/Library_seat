package com.petsaas.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.core.dto.OrderCreateDTO;
import com.petsaas.core.entity.ProductOrder;

import java.util.List;

public interface ProductOrderService extends IService<ProductOrder> {

    /**
     * 创建订单
     */
    String createOrder(Long userId, OrderCreateDTO dto);

    /**
     * 获取用户订单
     */
    List<ProductOrder> getUserOrders(Long userId, String status);

    /**
     * 支付订单
     */
    boolean payOrder(Long orderId);
}