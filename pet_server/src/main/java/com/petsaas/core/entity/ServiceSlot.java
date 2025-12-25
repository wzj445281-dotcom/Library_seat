package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("service_station") // [修复] 修正为 SQL 中的真实表名
public class ServiceSlot {
    private Long id;

    @TableField("label") // SQL中是 label, 对应 Entity 的 name
    private String name;

    private Integer serviceType; // SQL中是 int

    @TableField("base_price") // SQL中是 base_price
    private BigDecimal price;

    private Integer status; // 1可用 0维修

    @TableField(exist = false)
    private Double busyScore;
}