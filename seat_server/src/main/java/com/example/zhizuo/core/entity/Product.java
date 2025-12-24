package com.example.zhizuo.core.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product implements Serializable {
    @ExcelIgnore
    @TableId(type = IdType.AUTO)
    private Long id;

    @ExcelProperty("商品名称")
    private String name;

    /**
     * 售价
     */
    @ExcelProperty("售价")
    private BigDecimal price;

    /**
     * 库存
     */
    @ExcelProperty("库存")
    private Integer stock;

    /**
     * 0=主粮, 1=零食, 2=玩具, 3=医疗
     */
    @ExcelProperty("分类(0主粮1零食2玩具3医疗)")
    private Integer category;

    /**
     * 商品图片
     */
    @ExcelIgnore
    private String imageUrl;

    /**
     * 商品详情
     */
    @ExcelIgnore
    private String description;

    @ExcelIgnore
    private Integer status;

    @ExcelIgnore
    private Integer version;

    @ExcelIgnore
    private LocalDateTime createTime;

    @ExcelIgnore
    private LocalDateTime updateTime;
}