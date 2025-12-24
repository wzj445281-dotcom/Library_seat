package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.WorkOrder;

/**
 * 工单服务接口
 */
public interface WorkOrderService extends IService<WorkOrder> {
    /**
     * 提交智能工单
     * @param userId 用户ID
     * @param category 分类
     * @param content 内容
     * @param imgUrl 图片地址
     */
    void submitTicket(Long userId, String category, String content, String imgUrl);
}