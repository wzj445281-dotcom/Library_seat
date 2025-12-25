package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long seatId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 状�? RESERVED(已预�?, CHECKED_IN(已签�?, COMPLETED(已结�?, CANCELLED(已取�?, VIOLATION(违约)
    private String status;

    // 签到时间
    private LocalDateTime checkInTime;

    // 创建时间 (Day 2 新增，用于记录订单生成时�?
    private LocalDateTime createTime;
}