package com.petsaas.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.ServiceSlot;
import com.petsaas.core.mapper.ServiceSlotMapper;
import com.petsaas.core.service.AdminServiceSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminServiceSlotServiceImpl extends ServiceImpl<ServiceSlotMapper, ServiceSlot> implements AdminServiceSlotService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSlot(ServiceSlot slot) {
        // 检查名称是否重复
        Long count = this.count(new QueryWrapper<ServiceSlot>().eq("name", slot.getName()));
        if (count > 0) {
            throw new RuntimeException("工位名称已存在");
        }

        slot.setCreateTime(LocalDateTime.now());
        slot.setStatus(1); // 默认启用
        this.save(slot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSlot(ServiceSlot slot) {
        ServiceSlot old = this.getById(slot.getId());
        if (old == null) {
            throw new RuntimeException("工位不存在");
        }
        slot.setUpdateTime(LocalDateTime.now());
        this.updateById(slot);
    }
}