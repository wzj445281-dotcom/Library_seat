package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 书籍/商品实体
 * 对应数据库表: products
 */
@Data
@TableName("products")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer sales;

    private String imgUrl;

    /**
     * 1=上架, 0=下架
     */
    private Integer status;

    private LocalDateTime createTime;
}