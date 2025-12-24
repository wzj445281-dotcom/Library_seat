package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.ResourceOrder;

/**
 * 资源/积分兑换订单服务接口
 */
public interface ResourceOrderService extends IService<ResourceOrder> {

    /**
     * 创建物资借用订单
     * @param userId 用户ID
     * @param resourceId 物资ID
     * @param deliveryType 0=自取, 1=配送到座
     * @return 订单号
     */
    String createOrder(Long userId, Long resourceId, Integer deliveryType);

    /**
     * 归还物资并结算
     * @param orderNo 订单号
     */
    void returnResource(String orderNo);
}