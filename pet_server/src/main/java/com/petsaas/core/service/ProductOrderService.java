package com.petsaas.core.service;

public interface ProductOrderService {
    /**
     * 创建商品订单
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 购买数量
     * @return 订单�?     */
    String createOrder(Long userId, Long productId, Integer quantity);
}