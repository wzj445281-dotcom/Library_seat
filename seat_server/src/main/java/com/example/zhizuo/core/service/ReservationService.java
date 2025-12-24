package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.ServiceBooking;
import com.example.zhizuo.core.entity.ServiceSlot;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService extends IService<ServiceBooking> {
    // 获取可用工位
    List<ServiceSlot> getAvailableSlots(String type);

    // 创建预约
    void createBooking(Long userId, Long slotId, String petName, LocalDateTime appointmentTime);

    // 获取我的预约
    List<ServiceBooking> getMyBookings(Long userId);
    
    // 取消预约
    void cancelBooking(Long bookingId);
}