package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("seat_heat_stats")
public class SeatHeatStats {
    private Long id;
    private Long seatId;
    private Double heatScore;
    private LocalDate predictionDate;
    private LocalDateTime updateTime;
}