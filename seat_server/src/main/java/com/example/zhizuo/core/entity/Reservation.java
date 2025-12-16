package com.example.zhizuo.core.entity;

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

    // 状态: RESERVED(已预约), CHECKED_IN(已签到), COMPLETED(已结束), CANCELLED(已取消)
    private String status;

    // 新增字段：签到时间
    private LocalDateTime checkInTime;
}