package com.example.zhizuo.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.ServiceBooking;
import com.example.zhizuo.core.entity.ServiceSlot;
import com.example.zhizuo.core.mapper.ServiceBookingMapper;
import com.example.zhizuo.core.mapper.ServiceSlotMapper;
import com.example.zhizuo.core.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ServiceBookingMapper, ServiceBooking> implements ReservationService {

    @Autowired
    private ServiceSlotMapper serviceSlotMapper;

    @Override
    public List<ServiceSlot> getAvailableSlots(String type) {
        QueryWrapper<ServiceSlot> query = new QueryWrapper<>();
        query.eq("status", 1); // 只查询可用的工位
        if (type != null && !type.isEmpty()) {
            query.eq("service_type", type);
        }
        return serviceSlotMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBooking(Long userId, Long slotId, String petName, LocalDateTime appointmentTime) {
        // 1. 检查工位是否存在且可用
        ServiceSlot slot = serviceSlotMapper.selectById(slotId);
        if (slot == null || slot.getStatus() != 1) {
            throw new RuntimeException("工位不存在或不可用");
        }

        // 2. 检查是否已有相同时间的预约
        QueryWrapper<ServiceBooking> query = new QueryWrapper<>();
        query.eq("slot_id", slotId)
             .eq("appointment_time", appointmentTime)
             .in("status", "PENDING", "CONFIRMED");
        
        ServiceBooking existingBooking = baseMapper.selectOne(query);
        if (existingBooking != null) {
            throw new RuntimeException("该时间段已有预约");
        }

        // 3. 创建预约
        ServiceBooking booking = new ServiceBooking();
        booking.setUserId(userId);
        booking.setSlotId(slotId);
        booking.setPetName(petName);
        booking.setAppointmentTime(appointmentTime);
        booking.setStatus("PENDING");
        booking.setCreateTime(LocalDateTime.now());
        
        this.save(booking);
    }

    @Override
    public List<ServiceBooking> getMyBookings(Long userId) {
        QueryWrapper<ServiceBooking> query = new QueryWrapper<>();
        query.eq("user_id", userId)
             .orderByDesc("create_time");
        return this.list(query);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId) {
        ServiceBooking booking = this.getById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约不存在");
        }
        
        if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("只能取消待确认或已确认的预约");
        }
        
        booking.setStatus("CANCELLED");
        this.updateById(booking);
    }
}