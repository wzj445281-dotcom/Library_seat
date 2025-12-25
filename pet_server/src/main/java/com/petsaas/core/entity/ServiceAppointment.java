package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_appointment")
public class ServiceAppointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    
    /**
     * 关联宠物
     */
    private Long petId;
    
    /**
     * 工位ID
     */
    private Long stationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 状�? UNPAID(待支�?, BOOKED(已预�?, IN_PROGRESS(服务�?, COMPLETED(已完�?, CANCELLED(已取�?
    private String status;

    // 签到时间
    private LocalDateTime checkInTime;
    
    /**
     * 服务总价
     */
    private BigDecimal totalPrice;

    // 创建时间
    private LocalDateTime createTime;
}