package com.petsaas.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.core.entity.ProductOrder;

public interface ProductOrderService extends IService<ProductOrder> {

    // 创建订单
    ProductOrder createOrder(Long userId, Long productId, Integer quantity);

    // 支付
    void payOrder(Long userId, String orderNo);

    // 查询我的订单
    IPage<ProductOrder> getMyOrders(Page<ProductOrder> page, Long userId, String status);
}