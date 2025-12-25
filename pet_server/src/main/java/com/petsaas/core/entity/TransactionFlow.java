package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@TableName("transaction_flow")
public class TransactionFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String type; // RECHARGE, PAYMENT, REFUND
    private String orderNo;
    private String description;
    private LocalDateTime createTime;

    @Tolerate
    public TransactionFlow() {}
}