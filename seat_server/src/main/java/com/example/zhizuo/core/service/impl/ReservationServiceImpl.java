package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.ServiceBooking;
import com.example.zhizuo.core.entity.ServiceSlot;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ServiceBookingMapper;
import com.example.zhizuo.core.mapper.ServiceSlotMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationServiceImpl extends ServiceImpl<ServiceBookingMapper, ServiceBooking> implements ReservationService {

    @Autowired private ServiceSlotMapper slotMapper;
    @Autowired private ServiceBookingMapper bookingMapper;
    @Autowired private UserMapper userMapper;

    @Override
    public List<ServiceSlot> getAvailableSlots(String type) {
        return slotMapper.findAvailableByType(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBooking(Long userId, Long slotId, String petName, LocalDateTime appointmentTime) {
        // 1. 检查工位
        ServiceSlot slot = slotMapper.selectById(slotId);
        if (slot == null || slot.getStatus() == 0) {
            throw new RuntimeException("该工位暂时不可预约");
        }

        // 2. 检查时间冲突 (简单示例: 同一小时内只能约一次)
        String timeStr = appointmentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00"));
        // 注意：实际项目中需要更复杂的时间段重叠判断
        int conflicts = bookingMapper.checkConflict(slotId, timeStr);
        if (conflicts > 0) {
            throw new RuntimeException("该时间段已被预约，请更换时间");
        }

        // 3. 检查余额 
        User user = userMapper.selectById(userId); 
        if (user.getBalance().compareTo(slot.getBasePrice()) < 0) { 
            throw new RuntimeException("余额不足以支付服务费"); 
        } 
 
        // 4. 创建预约单 
        ServiceBooking booking = new ServiceBooking(); 
        booking.setUserId(userId); 
        booking.setSlotId(slotId); 
        booking.setPetName(petName); 
        booking.setAppointmentTime(appointmentTime); 
        booking.setStatus("CONFIRMED"); 
        booking.setBookingNo(UUID.randomUUID().toString().replace("-", "").substring(0, 12)); 
        booking.setTotalPrice(slot.getBasePrice()); 
        booking.setDurationMinutes(60); // 默认服务时长1小时 
        booking.setCreateTime(LocalDateTime.now()); 
        
        bookingMapper.insert(booking); 
 
        // 5. 扣费 (或由线下支付，此处演示预扣费) 
        userMapper.deductBalance(userId, slot.getBasePrice().doubleValue());
    }

    @Override
    public List<ServiceBooking> getMyBookings(Long userId) {
        return bookingMapper.findByUserId(userId);
    }

    @Override 
    public void cancelBooking(Long bookingId) { 
        ServiceBooking booking = bookingMapper.selectById(bookingId); 
        if (booking != null && !"CANCELLED".equals(booking.getStatus())) { 
            booking.setStatus("CANCELLED"); 
            bookingMapper.updateById(booking); 
            // 这里可以加上退款逻辑 
        } 
    }
}