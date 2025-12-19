package com.example.zhizuo.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private UserMapper userMapper;

    // ... 其他依赖注入 (RedisTemplate, RedissonClient 等保持原样) ...

    /**
     * [新增] App端专用入口：通过 studentId 进行预约
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(String studentId, Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 在 Service 内部完成用户查找，不再依赖 Controller
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new RuntimeException("用户不存在，无法预约");
        }

        // 2. 调用核心预约逻辑 (复用已有的 userId 逻辑)
        this.reserve(user.getId(), seatId, startTime, endTime);
    }

    /**
     * 核心预约逻辑 (原有逻辑保持不变)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        // ... 这里是你原有的 Redisson 锁、库存检查、死信队列发送等核心代码 ...
        // 请确保这部分保留你原本的高并发处理逻辑
        // 下面仅为示意：
        /*
        RLock lock = redissonClient.getLock("seat_lock:" + seatId);
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                // 检查冲突...
                // 保存订单...
            } else {
                throw new RuntimeException("抢座人数过多，请稍后重试");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("系统繁忙");
        } finally {
            lock.unlock();
        }
        */
    }

    @Override
    public List<Reservation> getUserReservations(String studentId) {
        // 原有逻辑应该已经实现了这个，这里保持接口一致性
        return baseMapper.selectList(new QueryWrapper<Reservation>()
                .eq("student_id", studentId) // 假设 Reservation 表里存了 student_id 或者关联查询
                .orderByDesc("create_time"));
    }

    @Override
    public void checkIn(Long reservationId) {
        // 实现签到逻辑
        Reservation reservation = getById(reservationId);
        if (reservation == null) throw new RuntimeException("预约不存在");
        // ... 状态更新 ...
        updateById(reservation);
    }

    @Override
    public void cancel(Long reservationId) {
        // 实现取消逻辑
        removeById(reservationId);
    }

    @Override
    public void leave(Long reservationId) {
        // 实现离座逻辑
        Reservation reservation = getById(reservationId);
        if (reservation != null) {
            reservation.setStatus(2); // 假设 2 代表完成
            updateById(reservation);
        }
    }
}