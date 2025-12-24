package com.petsaas.common.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_service")
public class Service extends BaseEntity {
    private Long merchantId;
    private String name;
    private String category;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private String description;
    private String coverImg;
    private Integer status;
}