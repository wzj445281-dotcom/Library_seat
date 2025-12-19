package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService extends IService<Reservation> {

    // 原有核心方法（Admin或内部调用）
    void reserve(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime);

    // [新增] App端专用方法：支持直接传 studentId
    void reserve(String studentId, Long seatId, LocalDateTime startTime, LocalDateTime endTime);

    List<Reservation> getUserReservations(String studentId);

    void checkIn(Long reservationId);

    void cancel(Long reservationId);

    void leave(Long reservationId);
}