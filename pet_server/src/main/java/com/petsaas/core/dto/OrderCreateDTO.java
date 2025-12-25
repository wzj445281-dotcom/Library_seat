package com.petsaas.core.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderCreateDTO {
    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 订单总金额 (前端计算传入，后端需校验，这里暂信前端)
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;
}