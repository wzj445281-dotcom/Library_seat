package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.ServiceSlot;

import java.util.List;

/**
 * 管理端服务工位服务接口
 * 继承 IService 以获得 MyBatis Plus 的基础 CRUD 能力
 */
public interface AdminServiceSlotService extends IService<ServiceSlot> {

    /**
     * 批量生成服务工位
     * @param count 工位数量
     */
    void batchCreateServiceSlots(int count);

    /**
     * 获取所有服务工位列表
     * @return 服务工位列表
     */
    List<ServiceSlot> getAllServiceSlots();
}