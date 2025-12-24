package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("service_station") // 对应数据库表名
public class ServiceStation {
    private Long id;
    private String label;
    private Integer gridX;
    private Integer gridY;
    
    /**
     * 1=洗澡位, 2=美容台, 3=诊疗室
     */
    private Integer serviceType;
    
    /**
     * 基础服务费
     */
    private BigDecimal basePrice;
    
    /**
     * 1可用 0维修
     */
    private Integer status;

    @TableField(exist = false)
    private Double busyScore;
}