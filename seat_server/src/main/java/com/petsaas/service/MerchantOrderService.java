package com.petsaas.service;

import com.petsaas.common.domain.Order;

public interface MerchantOrderService {
    /**
     * 核销订单
     * @param merchantId 商户ID
     * @param verifyCode 核销码
     */
    void verifyOrder(Long merchantId, String verifyCode);
}