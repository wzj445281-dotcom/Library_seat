package com.petsaas.core.dto;

import lombok.Data;

@Data
public class ReservationRequestDTO {
    private Long doctorId;

    // 前端传来的是 "yyyy-MM-dd HH:mm:ss" 格式字符串
    private String startTime;

    private String note;
}