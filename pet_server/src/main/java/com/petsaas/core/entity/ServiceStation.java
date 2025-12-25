package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("service_station") // 对应数据库表�?public class ServiceStation {
    private Long id;
    private String label;
    private Integer gridX;
    private Integer gridY;
    
    /**
     * 1=洗澡�? 2=美容�? 3=诊疗�?     */
    private Integer serviceType;
    
    /**
     * 基础服务�?     */
    private BigDecimal basePrice;
    
    /**
     * 1可用 0维修
     */
    private Integer status;

    @TableField(exist = false)
    private Double busyScore;
}