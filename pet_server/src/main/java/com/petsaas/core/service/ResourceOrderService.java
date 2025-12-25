package com.petsaas.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.core.entity.ResourceOrder;

/**
 * 资源/积分兑换订单服务接口
 */
public interface ResourceOrderService extends IService<ResourceOrder> {

    /**
     * 创建物资借用订单
     * @param userId 用户ID
     * @param resourceId 物资ID
     * @param deliveryType 0=自取, 1=配送到�?
     * @return 订单�?
     */
    String createOrder(Long userId, Long resourceId, Integer deliveryType);

    /**
     * 归还物资并结�?
     * @param orderNo 订单�?
     */
    void returnResource(String orderNo);
}