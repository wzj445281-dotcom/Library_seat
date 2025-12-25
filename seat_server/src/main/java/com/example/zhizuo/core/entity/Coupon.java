package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板实体
 */
@Data
@Accessors(chain = true)
@TableName("coupon")
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    private String title;

    /**
     * 优惠金额
     */
    private BigDecimal amount;

    /**
     * 使用门槛
     */
    private BigDecimal minPoint;

    /**
     * 剩余库存
     */
    private Integer stock;

    /**
     * 总库存
     */
    private Integer totalStock;

    /**
     * 状态 1:上架 0:下架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}