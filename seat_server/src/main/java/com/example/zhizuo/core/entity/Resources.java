package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("resources")
public class Resources implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 0=图书, 1=雨伞, 2=充电宝
     */
    private Integer type;

    private Integer stock;

    private Integer totalStock;

    private String location;

    /**
     * 每小时消耗积分
     */
    private Integer hourlyCost;

    private String imgUrl;

    /**
     * 1上架 0下架
     */
    private Integer status;

    private Integer version;
}