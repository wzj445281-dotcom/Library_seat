package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联用户
     */
    private Long userId;
    
    /**
     * 宠物�?     */
    private String name;
    
    /**
     * 0=�? 1=�?     */
    private Integer type;
    
    /**
     * 体重(用于计算洗澡价格)
     */
    private BigDecimal weight;
    
    /**
     * 生日
     */
    private LocalDate birthday;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}