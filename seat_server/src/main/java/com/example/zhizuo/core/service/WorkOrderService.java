package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.WorkOrder;

/**
 * 工单服务接口
 */
public interface WorkOrderService extends IService<WorkOrder> {
    // 这里继承了 IService，已经包含了基本的增删改查方法
    // 如果 Controller 中调用了特有的业务方法（如 submit, handle），可以在这里补充定义
}