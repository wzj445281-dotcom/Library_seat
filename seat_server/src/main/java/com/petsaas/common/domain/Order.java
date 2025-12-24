package com.petsaas.common.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_order")
public class Order extends BaseEntity {
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Integer orderType; // 0-商品订单 1-服务预约
    
    // 服务预约特有字段 (如果是商品订单则为空)
    private Long petId;
    private Long scheduleId;
    private LocalDateTime appointmentTime;
    
    // 通用金额字段
    private BigDecimal totalAmount;
    private Integer payStatus; // 0-待支付 1-已支付 2-已退款
    private Integer orderStatus; // 0-待核销/发货 1-已完成 2-已取消
    
    private String remark;
    
    // 核销码，用于用户到店消费凭证
    private String verifyCode;
}