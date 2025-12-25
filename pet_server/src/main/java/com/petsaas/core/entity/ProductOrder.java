package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_product_order")
public class ProductOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long merchantId;

    private BigDecimal totalAmount;
    private String status; // PENDING, PAID, SHIPPED, COMPLETED, CANCELLED

    private LocalDateTime createTime;
    private LocalDateTime payTime;
}