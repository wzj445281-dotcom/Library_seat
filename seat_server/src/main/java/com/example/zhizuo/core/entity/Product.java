package com.example.zhizuo.core.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Product {
    private Long id;
    private String name;      // 商品名
    private String category;  // 分类：FOOD, TOY
    private BigDecimal price; // 价格
    private Integer stock;    // 库存
    private Integer sales;    // 销量
    private String imgUrl;    // 图片链接
    private String detail;    // 详情
    private Integer status;   // 1上架 0下架
    private Date createTime;
    private Date updateTime;
}
