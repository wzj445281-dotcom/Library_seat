package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * O2O 销售订单主表
 * 对应数据库表: orders
 */
@Data
@TableName("orders")
public class Order implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    /**
     * 0=未支付, 1=已支付, 2=已退款
     */
    private Integer payStatus;

    /**
     * 状态机: PENDING(待支付) -> PAID(制作/配货中) -> READY(待取/配送) -> COMPLETED(完成) -> CANCELLED(取消)
     */
    private String status;

    /**
     * 0=门店自取, 1=外卖配送
     */
    private Integer deliveryType;

    private String pickupCode;

    private String addressInfo;

    /**
     * 使用的优惠券ID (UserCoupon.id)
     */
    private Long couponId;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 订单原价（优惠前）
     */
    private BigDecimal originalAmount;

    // ✅ 【关键修复】格式化时间，防止前端 JS 解析报错
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;

    // ✅ 【关键修复】格式化时间，防止前端 JS 解析报错
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // --- 非数据库字段，用于前端展示 ---
    @TableField(exist = false)
    private List<OrderItem> products; // 订单包含的商品列表

    @TableField(exist = false)
    private String userName; // 下单人姓名
}