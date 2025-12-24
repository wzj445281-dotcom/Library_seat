package com.petsaas.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.common.domain.ServiceSchedule;
import com.petsaas.dto.BatchPublishReq;
import com.petsaas.mapper.ServiceScheduleMapper;
import com.petsaas.service.ServiceScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceScheduleServiceImpl extends ServiceImpl<ServiceScheduleMapper, ServiceSchedule> implements ServiceScheduleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchPublish(Long merchantId, BatchPublishReq req) {
        // req 包含: serviceId, startDate, endDate, dailyStartTime, dailyEndTime, capacity
        
        List<ServiceSchedule> newSchedules = new ArrayList<>();
        
        // 1. 遍历日期 (例如: 2025-12-25 到 2025-12-31)
        LocalDate current = req.getStartDate();
        while (!current.isAfter(req.getEndDate())) {
            
            // 2. 遍历时间段 (例如: 10:00, 11:00... 直到 18:00)
            LocalTime timeSlot = req.getDailyStartTime();
            while (timeSlot.isBefore(req.getDailyEndTime())) {
                
                // 构建排期对象
                ServiceSchedule schedule = new ServiceSchedule();
                schedule.setMerchantId(merchantId); // 多租户隔离
                schedule.setServiceId(req.getServiceId());
                schedule.setScheduleDate(current);
                schedule.setStartTime(timeSlot);
                schedule.setEndTime(timeSlot.plusMinutes(60)); // 假设每小时一班，可灵活配置
                schedule.setMaxCapacity(req.getCapacity()); // 例如: 3 (意味着该时段能接3单)
                schedule.setBookedCount(0);
                schedule.setStatus(1);
                
                newSchedules.add(schedule);
                
                // 时间步进 1小时
                timeSlot = timeSlot.plusHours(1);
            }
            // 日期步进 1天
            current = current.plusDays(1);
        }
        
        // 3. 批量插入数据库 (使用 MyBatis-Plus 的 saveBatch 提高性能)
        this.saveBatch(newSchedules);
    }
}