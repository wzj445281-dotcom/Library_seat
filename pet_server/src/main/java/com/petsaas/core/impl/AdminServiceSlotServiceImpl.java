package com.petsaas.core.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.ServiceSlot;
import com.petsaas.core.mapper.ServiceSlotMapper;
import com.petsaas.core.service.AdminServiceSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminServiceSlotServiceImpl extends ServiceImpl<ServiceSlotMapper, ServiceSlot> implements AdminServiceSlotService {

    // 继承 ServiceImpl 后，baseMapper 就是 ServiceSlotMapper，可以直接用

    @Override
    @Transactional(rollbackFor = Exception.class) // 确保批量操作要么全成功，要么全失�?    public void batchCreateServiceSlots(int count) {
        // 模拟一点复杂的校验逻辑，增加代码量
        if (count <= 0) {
            throw new IllegalArgumentException("工位数量不合�?);
        }

        // 这里的逻辑和之前一样，但搬到了 Service �?        for (int i = 1; i <= count; i++) {
            ServiceSlot slot = new ServiceSlot();
            slot.setName("工位" + i);
            
            // 根据编号分配不同类型的服�?            if (i % 3 == 0) {
                slot.setServiceType("Basic Bath");
                slot.setPrice(new BigDecimal("50.00"));
            } else if (i % 3 == 1) {
                slot.setServiceType("Premium Grooming");
                slot.setPrice(new BigDecimal("120.00"));
            } else {
                slot.setServiceType("Medical Checkup");
                slot.setPrice(new BigDecimal("200.00"));
            }
            
            slot.setStatus(1); // 默认可用
            // 使用 ServiceImpl 提供�?save 方法
            this.save(slot);
        }
    }

    @Override
    public List<ServiceSlot> getAllServiceSlots() {
        // 可以在这里加缓存逻辑 (Redis)，以后升级用
        return this.list();
    }
}