package com.example.zhizuo.core.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceBooking {
    private Long id;

    private String bookingNo; // 预约单号 (UUID)

    private Long userId;

    // 原本是 seatId
    private Long slotId;      // 预约的工位ID

    // 新增宠物信息
    private String petName;
    private String petType;   // DOG, CAT

    // 时间字段
    private LocalDateTime appointmentTime; // 预约时间点
    private Integer durationMinutes; // 预计耗时

    private String status;    // PENDING, CONFIRMED

    private BigDecimal totalPrice; // 订单金额

    private LocalDateTime createTime;
    
    // 添加缺失的方法
    public LocalDateTime getStartTime() {
        return appointmentTime;
    }
    
    public LocalDateTime getEndTime() {
        if (appointmentTime != null && durationMinutes != null) {
            return appointmentTime.plusMinutes(durationMinutes);
        }
        return null;
    }
    
    public Long getSeatId() {
        return slotId;
    }
    
    public LocalDateTime getCheckInTime() {
        return appointmentTime; // 使用appointmentTime作为签到时间
    }
}