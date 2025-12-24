package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("service_slots") // 对应数据库表名
public class ServiceSlot {
    private Long id;
    private String name;
    private String serviceType; // 例如 "Basic Bath", "Premium Grooming"
    private BigDecimal price; // 服务单价
    private Integer status; // 1可用 0维修

    @TableField(exist = false)
    private Double busyScore;
}