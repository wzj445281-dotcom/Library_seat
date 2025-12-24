package com.petsaas.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderReq {
    private Long scheduleId;
    private Long petId;
    private BigDecimal price;
}