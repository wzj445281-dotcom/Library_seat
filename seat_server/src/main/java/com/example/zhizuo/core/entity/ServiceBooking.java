package com.example.zhizuo.core.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceBooking {
    private Long id;

    private String bookingNo; // 预约单号 (UUID)

    private Long userId;

    // 预约的座位ID
    private Long slotId;      // 座位ID（对应seat表）

    // 时间字段
    private LocalDateTime appointmentTime; // 预约时间点
    private Integer durationMinutes; // 预计耗时（分钟）

    private String status;    // PENDING(待确认), RESERVED(已预约), CHECKED_IN(已签到), COMPLETED(已完成), CANCELLED(已取消)

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