package com.petsaas.common.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_service_schedule")
public class ServiceSchedule extends BaseEntity {
    // 核心：多租户ID
    private Long merchantId;
    
    private Long serviceId; // 关联的服务ID (如: 精洗)
    
    private LocalDate scheduleDate; // 日期: 2025-12-25
    private LocalTime startTime;    // 开始: 10:00
    private LocalTime endTime;      // 结束: 11:00
    
    private Integer maxCapacity; // 最大接待量 (原座位数)
    private Integer bookedCount; // 已预约数
    private Integer status;      // 1-可约
    
    // 兼容方法，为了解决编译错误
    public Integer getCapacity() {
        return maxCapacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.maxCapacity = capacity;
    }
    
    public Integer getBookedCount() {
        return bookedCount;
    }
    
    public void setBookedCount(Integer bookedCount) {
        this.bookedCount = bookedCount;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
}