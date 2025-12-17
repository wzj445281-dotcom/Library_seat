package com.example.zhizuo.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.mq.ReservationProducer;
import com.example.zhizuo.core.service.AiRiskService;
import com.example.zhizuo.core.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final UserMapper userMapper;
    private final RedissonClient redissonClient;
    private final ReservationProducer reservationProducer;
    // 1. 新增：注入 AI 风控服务
    private final AiRiskService aiRiskService;

    // 2. 修改：构造函数添加 AiRiskService 参数
    public ReservationServiceImpl(UserMapper userMapper,
                                  RedissonClient redissonClient,
                                  ReservationProducer reservationProducer,
                                  AiRiskService aiRiskService) {
        this.userMapper = userMapper;
        this.redissonClient = redissonClient;
        this.reservationProducer = reservationProducer;
        this.aiRiskService = aiRiskService;
    }

    @Override
    public List<Reservation> getUserReservations(String studentId) {
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("student_id", studentId);
        User user = userMapper.selectOne(userQuery);
        if (user == null) return Collections.emptyList();

        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("user_id", user.getId());
        query.orderByDesc("create_time");
        return this.list(query);
    }

    @Override
    public void reserve(Long userId, Long seatId, LocalDateTime start, LocalDateTime end) {
        // 1. 基础校验
        if (start.isBefore(LocalDateTime.now())) throw new RuntimeException("不能预约过去的时间");
        if (end.isBefore(start)) throw new RuntimeException("结束时间错误");
        if (Duration.between(start, end).toHours() > 4) throw new RuntimeException("不能超过4小时");

        User user = userMapper.selectById(userId);
        if (user.getCreditScore() < 60) throw new RuntimeException("信用分过低");

        // 3. 新增：插入 AI 风控逻辑 (在加锁之前判断)
        boolean isHighRisk = aiRiskService.isHighRiskUser(user.getCreditScore());
        if (isHighRisk) {
            log.warn("用户 {} 被 AI 判定为高风险用户", userId);
            // 这里我们选择记录日志，也可以选择直接抛异常拦截：
            // throw new RuntimeException("AI风控警告：您的履约风险过高，暂无法预约");
        }

        // 4. 加分布式锁
        String lockKey = "seat_lock:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    // --- 进入临界区 ---
                    int conflict = baseMapper.countConflict(seatId, start, end);
                    if (conflict > 0) {
                        throw new RuntimeException("手慢了，该座位已被抢订");
                    }

                    Reservation r = new Reservation();
                    r.setUserId(userId);
                    r.setSeatId(seatId);
                    r.setStartTime(start);
                    r.setEndTime(end);
                    r.setStatus("RESERVED");
                    r.setCreateTime(LocalDateTime.now());

                    this.save(r);

                    // 发送延迟消息
                    reservationProducer.sendDelayMessage(r.getId());

                    log.info("用户 {} 预约成功，消息已发送", userId);

                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("系统繁忙，请重试");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("系统异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null || !"RESERVED".equals(r.getStatus())) {
            throw new RuntimeException("无法签到");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(r.getStartTime().minusMinutes(15))) {
            throw new RuntimeException("未到签到时间");
        }

        r.setStatus("CHECKED_IN");
        r.setCheckInTime(now);
        this.updateById(r);
        updateUserCredit(r.getUserId(), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null || !"RESERVED".equals(r.getStatus())) throw new RuntimeException("无法取消");

        r.setStatus("CANCELLED");
        this.updateById(r);

        long minutes = Duration.between(LocalDateTime.now(), r.getStartTime()).toMinutes();
        if (minutes < 30 && minutes >= 0) {
            updateUserCredit(r.getUserId(), -2);
        }
    }

    @Override
    public void leave(Long reservationId) {
        // 即使是空方法，也保留在这里
    }

    @Override
    public void processNoShows() {
        // 保留空实现或兜底逻辑
    }

    private void updateUserCredit(Long userId, int delta) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            int newScore = Math.max(0, Math.min(110, user.getCreditScore() + delta));
            user.setCreditScore(newScore);
            userMapper.updateById(user);
        }
    }
}