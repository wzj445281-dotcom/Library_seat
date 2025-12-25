package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resource_orders")
public class ResourceOrder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long resourceId;

    private Long seatId;

    /**
     * 0=自取, 1=配送到�?
     */
    private Integer deliveryType;

    /**
     * CREATED, PICKING, DELIVERING, USING, RETURNED
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime startUseTime;

    private LocalDateTime returnTime;

    private Integer totalCost;
}