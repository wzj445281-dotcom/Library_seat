package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("service_appointment") // [修复] 修正为 SQL 中的真实表名
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;

    // 注意：SQL中是 station_id, 这里需要映射一下，或者保持字段名一致
    // 如果你的 SQL 字段是 station_id，建议这里改名或加注解
    // 假设你保持 seatId 这个属性名，则需要:
    @com.baomidou.mybatisplus.annotation.TableField("station_id")
    private Long seatId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime checkInTime;
    private LocalDateTime createTime;
}