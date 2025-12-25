package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细表
 * 对应数据库表: order_items
 */
@Data
@TableName("order_items")
public class OrderItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;
}