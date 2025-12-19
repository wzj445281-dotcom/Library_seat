package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.ResourceOrder;

/**
 * 资源/积分兑换订单服务接口
 */
public interface ResourceOrderService extends IService<ResourceOrder> {

    /**
     * 执行兑换逻辑
     * @param userId 用户ID
     * @param resourceId 资源ID
     */
    void exchange(Long userId, Long resourceId);
}