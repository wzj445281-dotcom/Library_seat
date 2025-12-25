package com.petsaas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.entity.Reservation;
import com.petsaas.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生排班核心业务逻辑
 * 用于计算“哪些时间段还能约”
 */
@Service
public class DoctorScheduleServiceImpl {

    @Autowired
    private ReservationMapper reservationMapper;

    // 定义营业时间：上午9点到下午6点，每30分钟一个号
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(18, 0);
    private static final int SLOT_MINUTES = 30;

    /**
     * 获取指定医生、指定日期的可用时间槽
     * @param doctorId 医生ID
     * @param date 日期字符串 (YYYY-MM-DD)
     * @return 可用的时间点列表 ["09:00", "09:30", ...]
     */
    public List<String> getAvailableSlots(Long doctorId, String date) {
        // 1. 生成当天所有标准时间槽 (09:00 - 18:00)
        List<String> allSlots = generateDailySlots();

        // 2. 查询该医生当天已存在的预约
        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("doctor_id", doctorId);
        query.eq("appointment_date", date);
        // 排除已取消的订单，避免误判
        query.ne("status", "CANCELLED");

        List<Reservation> bookedList = reservationMapper.selectList(query);

        // 3. 提取已被占用的时间点
        // 假设 Reservation 实体中有 getStartTime() 方法返回 String "HH:mm"
        List<String> bookedSlots = bookedList.stream()
                .map(Reservation::getStartTime)
                .collect(Collectors.toList());

        // 4. 从总列表中剔除被占用的时间槽
        allSlots.removeAll(bookedSlots);

        return allSlots;
    }

    /**
     * 辅助方法：生成标准时间槽
     */
    private List<String> generateDailySlots() {
        List<String> slots = new ArrayList<>();
        LocalTime current = START_TIME;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // 循环生成 09:00, 09:30 ... 直到 18:00
        while (current.isBefore(END_TIME)) {
            slots.add(current.format(formatter));
            current = current.plusMinutes(SLOT_MINUTES);
        }
        return slots;
    }
}