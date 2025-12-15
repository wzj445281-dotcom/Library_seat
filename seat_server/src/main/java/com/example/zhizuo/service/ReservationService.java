package com.example.zhizuo.service;

import com.example.zhizuo.entity.Reservation;
// ... existing code ...
import com.example.zhizuo.entity.User;
import com.example.zhizuo.mapper.ReservationMapper;
import com.example.zhizuo.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ReservationService {

    private final ReservationMapper mapper;
    private final UserMapper userMapper; // 新增 UserMapper 依赖

    // 修改构造函数注入
    public ReservationService(ReservationMapper mapper, UserMapper userMapper) {
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public void reserve(Long userId, Long seatId,
                        LocalDateTime start, LocalDateTime end) {
        // 1. 检查用户信用分
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getCreditScore() < 60) {
            throw new RuntimeException("信用分过低(" + user.getCreditScore() + ")，禁止预约");
        }

        // 2. 检查冲突 (原有逻辑)
        if (mapper.countConflict(seatId, start, end) > 0) {
            throw new RuntimeException("该时段座位已被占用"); // 修改报错信息更友好
        }

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setSeatId(seatId);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setStatus("RESERVED");
        mapper.insert(r);
    }

    @Transactional
    public void checkIn(Long reservationId) {
        Reservation r = mapper.selectById(reservationId);
        if (r == null) throw new RuntimeException("预约不存在");

        if (!"RESERVED".equals(r.getStatus())) {
            throw new RuntimeException("当前状态不可签到");
        }

        r.setStatus("CHECKED_IN");
        r.setCheckInTime(LocalDateTime.now()); // 假设 Entity 里加了这个字段，如果没有可先注释
        mapper.updateById(r);

        // 签到成功，信用分 +1 (上限 110)
        User user = userMapper.selectById(r.getUserId());
        if (user.getCreditScore() < 110) {
            user.setCreditScore(user.getCreditScore() + 1);
            userMapper.updateById(user);
        }
    }
// ... existing code ...