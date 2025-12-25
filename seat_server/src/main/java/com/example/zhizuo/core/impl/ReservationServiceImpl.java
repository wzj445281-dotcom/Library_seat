package com.example.zhizuo.core.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.ServiceBooking;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.mq.ReservationProducer;
import com.example.zhizuo.core.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, ServiceBooking> implements ReservationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Autowired(required = false)
    private ReservationProducer reservationProducer;

    /**
     * [新增] App端专用入口：通过 studentId 进行预约
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(String studentId, Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 在 Service 内部完成用户查找，不再依赖 Controller
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", studentId); // 修复：User表使用username字段，不是student_id
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new RuntimeException("用户不存在，无法预约");
        }

        // 2. 调用核心预约逻辑 (复用已有的 userId 逻辑)
        this.reserve(user.getId(), seatId, startTime, endTime);
    }

    /**
     * 核心预约逻辑
     * 
     * 实现流程：
     * 1. 参数校验
     * 2. 使用 Redisson 分布式锁防止并发超卖（锁 key: seat_lock:{seatId}）
     * 3. 检查座位在指定时间段是否已被预订（查询 service_booking 表）
     * 4. 如果可用，创建并保存 ServiceBooking 记录
     * 5. 发送 RabbitMQ 延迟消息到 order.delay.queue（用于订单超时自动取消）
     * 6. 释放锁
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 参数校验
        if (userId == null || seatId == null || startTime == null || endTime == null) {
            throw new RuntimeException("预约参数不完整");
        }
        if (startTime.isAfter(endTime) || startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("预约时间无效");
        }

        // 2. 使用分布式锁防止并发冲突（锁 key: seat_lock:{seatId}）
        String lockKey = "seat_lock:" + seatId;
        RLock lock = null;
        if (redissonClient != null) {
            lock = redissonClient.getLock(lockKey);
        }

        try {
            // 尝试获取锁：等待时间 0（不等待），锁定时间 10秒
            boolean locked = false;
            if (lock != null) {
                // tryLock(waitTime, leaseTime, timeUnit)
                // waitTime=0: 不等待，立即返回
                // leaseTime=10: 锁定10秒后自动释放
                locked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            } else {
                // 如果没有Redisson，直接执行（单机环境或开发环境）
                locked = true;
                log.warn("Redisson未配置，跳过分布式锁，可能存在并发风险");
            }

            if (!locked) {
                throw new RuntimeException("抢座人数过多，请稍后重试");
            }

            try {
                // 3. 检查座位在指定时间段是否已被预订（查询 service_booking 表）
                int conflictCount = baseMapper.countConflict(seatId, startTime, endTime);
                if (conflictCount > 0) {
                    throw new RuntimeException("该时间段已被预约，请选择其他时间");
                }

                // 4. 创建预约记录
                ServiceBooking booking = new ServiceBooking();
                booking.setBookingNo("RES" + IdUtil.getSnowflakeNextIdStr());
                booking.setUserId(userId);
                booking.setSlotId(seatId); // ServiceBooking使用slotId字段
                booking.setAppointmentTime(startTime);
                // 计算时长（分钟）
                long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
                booking.setDurationMinutes((int) durationMinutes);
                booking.setStatus("RESERVED");
                booking.setTotalPrice(BigDecimal.ZERO); // 可以根据业务设置价格
                booking.setCreateTime(LocalDateTime.now());

                // 5. 保存预约记录到数据库
                save(booking);

                // 6. 发送 RabbitMQ 延迟消息到 order.delay.queue（用于订单超时自动取消）
                if (reservationProducer != null) {
                    try {
                        reservationProducer.sendDelayMessage(booking.getId());
                        log.info("预约成功，已发送延迟消息到 order.delay.queue，预约ID: {}", booking.getId());
                    } catch (Exception e) {
                        log.error("发送延迟消息失败，预约ID: {}", booking.getId(), e);
                        // 消息发送失败不影响预约成功（异步操作，失败可后续补偿）
                    }
                } else {
                    log.warn("RabbitMQ未配置，跳过延迟消息发送");
                }

                log.info("预约成功：用户ID={}, 座位ID={}, 预约单号={}", userId, seatId, booking.getBookingNo());

            } finally {
                // 释放锁（确保在finally中释放，避免死锁）
                if (lock != null && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.debug("已释放分布式锁: {}", lockKey);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("系统繁忙，请稍后重试");
        } catch (RuntimeException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            log.error("预约失败：用户ID={}, 座位ID={}", userId, seatId, e);
            throw new RuntimeException("预约失败：" + e.getMessage());
        }
    }

    @Override
    public List<ServiceBooking> getUserReservations(String studentId) {
        // 1. 先通过studentId查找用户
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("username", studentId);
        User user = userMapper.selectOne(userQuery);
        
        if (user == null) {
            return Collections.emptyList(); // 用户不存在，返回空列表
        }
        
        // 2. 通过userId查询预约记录
        QueryWrapper<ServiceBooking> query = new QueryWrapper<>();
        query.eq("user_id", user.getId())
                .orderByDesc("create_time");
        return baseMapper.selectList(query);
    }

    @Override
    public void checkIn(Long reservationId) {
        ServiceBooking reservation = getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 检查状态
        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new RuntimeException("当前状态无法签到");
        }
        
        // 更新状态为已签到
        reservation.setStatus("CHECKED_IN");
        updateById(reservation);
        
        log.info("预约签到成功：预约ID={}", reservationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long reservationId) {
        ServiceBooking reservation = getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 检查是否可以取消
        if ("COMPLETED".equals(reservation.getStatus()) || "VIOLATION".equals(reservation.getStatus())) {
            throw new RuntimeException("当前状态无法取消");
        }
        
        // 更新状态为已取消
        reservation.setStatus("CANCELLED");
        updateById(reservation);
        
        log.info("预约取消成功：预约ID={}", reservationId);
    }

    @Override
    public void leave(Long reservationId) {
        ServiceBooking reservation = getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 检查状态
        if (!"CHECKED_IN".equals(reservation.getStatus())) {
            throw new RuntimeException("当前状态无法离座");
        }
        
        // 更新状态为已完成
        reservation.setStatus("COMPLETED");
        updateById(reservation);
        
        log.info("离座成功：预约ID={}", reservationId);
    }
}