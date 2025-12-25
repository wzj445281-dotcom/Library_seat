package com.petsaas.service;

import com.petsaas.core.entity.ServiceBooking;
import java.util.List;

public interface MerchantOrderService {
    /**
     * 核销订单 (支持商品订单号或服务预约号)
     * @param code 核销码/订单号
     * @return 是否成功
     */
    boolean verifyOrder(String code);

    /**
     * 获取今日待核销的服务预约
     */
    List<ServiceBooking> getPendingOrders();
}