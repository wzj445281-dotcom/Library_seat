package com.petsaas.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.common.domain.Order;
import com.petsaas.dto.CreateOrderReq;

public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     * @param req 创建订单请求
     * @return 订单ID
     */
    Long createOrder(CreateOrderReq req);
}