package com.petsaas.common.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_order_item")
public class OrderItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImg;
    private BigDecimal price;
    private Integer quantity;
}