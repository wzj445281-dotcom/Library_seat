package com.petsaas.common.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_product")
public class Product extends BaseEntity {
    private Long merchantId;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String imageUrls;
    private String detail;
    private Integer isRecommend;
    private Integer status;
}