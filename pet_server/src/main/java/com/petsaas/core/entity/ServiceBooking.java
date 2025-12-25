package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_service_booking")
public class ServiceBooking {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long slotId;
    private Long merchantId; // 所属商户ID

    private String bookingNo; // 预约号/核销码
    private String petName;
    private LocalDateTime appointmentTime; // 预约时间
    private Integer durationMinutes; // 服务时长

    private BigDecimal totalPrice;
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED, MISSED

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}