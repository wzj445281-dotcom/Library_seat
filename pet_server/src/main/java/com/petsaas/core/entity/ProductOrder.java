package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_orders")
public class ProductOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo; // 订单编号
    private Long userId; // 用户ID
    private BigDecimal totalAmount; // 订单总金�?    private String status; // 状�? UNPAID, PAID, SHIPPED, COMPLETED
    private String address; // 收货地址
    private LocalDateTime payTime; // 支付时间
    private LocalDateTime createTime; // 创建时间
}