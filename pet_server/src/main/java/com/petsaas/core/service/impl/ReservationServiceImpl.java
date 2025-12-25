package com.petsaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.dto.ReservationRequestDTO;
import com.petsaas.core.entity.Reservation;
import com.petsaas.core.mapper.ReservationMapper;
import com.petsaas.core.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createReservation(Long userId, ReservationRequestDTO dto) {
        // 1. 解析时间
        LocalDateTime start = LocalDateTime.parse(dto.getStartTime(), DF);
        LocalDateTime end = start.plusMinutes(30); // 默认一次问诊30分钟

        // 2. 简单的防冲突检查 (同一个医生在同一时间段不能有两个预约)
        // 注意：生产环境建议使用 Redis 分布式锁或数据库唯一索引(doctor_id + start_time) 防止并发
        Long count = this.baseMapper.selectCount(new QueryWrapper<Reservation>()
                .eq("doctor_id", dto.getDoctorId())
                .eq("start_time", start)
                .ne("status", "cancelled")); // 排除已取消的

        if (count > 0) {
            throw new RuntimeException("该时段已被预约，请选择其他时间");
        }

        // 3. 构建预约对象
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setDoctorId(dto.getDoctorId());
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus("confirmed"); // 默认为已确认，如果有支付流程则为 pending
        reservation.setCreateTime(LocalDateTime.now());

        // 这里的 seatId (station_id) 暂时留空，或者根据医生分配诊室
        // reservation.setSeatId(1L);

        return this.save(reservation);
    }

    @Override
    public List<Reservation> getUserReservations(Long userId) {
        // 按开始时间倒序排列
        return this.list(new QueryWrapper<Reservation>()
                .eq("user_id", userId)
                .orderByDesc("start_time"));
    }
}