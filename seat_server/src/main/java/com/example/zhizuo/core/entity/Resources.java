package com.example.zhizuo.core.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resources")
public class Resources implements Serializable {
    @ExcelIgnore
    @TableId(type = IdType.AUTO)
    private Long id;

    @ExcelProperty("资源名称")
    private String name;

    /**
     * 0=图书, 1=雨伞, 2=充电宝
     */
    @ExcelProperty("类型(0书1伞2电)")
    private Integer type;

    @ExcelProperty("ISBN")
    private String isbn;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty("出版社")
    private String publisher;

    @ExcelProperty("分类")
    private String category;

    @ExcelProperty("索书号/库位")
    private String locationCode;

    @ExcelProperty("初始库存")
    private Integer stock;

    @ExcelProperty("总库存")
    private Integer totalStock;

    @ExcelIgnore
    private String imgUrl;

    @ExcelIgnore
    private Integer status;

    @ExcelIgnore
    private Integer version;

    @ExcelIgnore
    private LocalDateTime createTime;

    @ExcelIgnore
    private LocalDateTime updateTime;
}