package com.example.zhizuo.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reservation {
    private Long id;
    private Long userId;
    private Long seatId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}