package com.example.zhizuo.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.CreditLog;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.CreditLogMapper;
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
    private final AiRiskService aiRiskService;
    // 新增：信用日志Mapper
    private final CreditLogMapper creditLogMapper;

    public ReservationServiceImpl(UserMapper userMapper,
                                  RedissonClient redissonClient,
                                  ReservationProducer reservationProducer,
                                  AiRiskService aiRiskService,
                                  CreditLogMapper creditLogMapper) {
        this.userMapper = userMapper;
        this.redissonClient = redissonClient;
        this.reservationProducer = reservationProducer;
        this.aiRiskService = aiRiskService;
        this.creditLogMapper = creditLogMapper;
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
        if (user.getCreditScore() < 60) throw new RuntimeException("信用分过低，无法预约");

        // 2. AI 风控
        boolean isHighRisk = aiRiskService.isHighRiskUser(user.getCreditScore());
        if (isHighRisk) {
            log.warn("用户 {} 被 AI 判定为高风险用户", userId);
            // 策略：高风险用户只能预约 2 小时以内
            if (Duration.between(start, end).toHours() > 2) {
                throw new RuntimeException("系统提示：因您的信用评分波动，暂只能预约2小时内的座位");
            }
        }

        // 3. 加分布式锁
        String lockKey = "seat_lock:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    // --- 临界区 ---
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

                    // 发送延迟消息 (用于处理超时未签到)
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
            throw new RuntimeException("订单状态不满足签到条件");
        }

        LocalDateTime now = LocalDateTime.now();
        // 允许提前 15 分钟签到
        if (now.isBefore(r.getStartTime().minusMinutes(15))) {
            throw new RuntimeException("还没到签到时间呢，再等等");
        }

        r.setStatus("CHECKED_IN");
        r.setCheckInTime(now);
        this.updateById(r);

        // 签到奖励
        updateUserCredit(r.getUserId(), 1, "准时签到奖励");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null || !"RESERVED".equals(r.getStatus())) throw new RuntimeException("无法取消");

        r.setStatus("CANCELLED");
        this.updateById(r);

        // 如果距离开始不足 30 分钟取消，扣分
        long minutes = Duration.between(LocalDateTime.now(), r.getStartTime()).toMinutes();
        if (minutes < 30 && minutes >= 0) {
            updateUserCredit(r.getUserId(), -2, "临期取消扣分");
        }
    }

    // 核心实现：结束使用 (离座)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leave(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null) throw new RuntimeException("订单不存在");

        // 只有“已签到”状态才能正常离座
        if (!"CHECKED_IN".equals(r.getStatus())) {
            throw new RuntimeException("您还没签到，无法正常离座");
        }

        r.setStatus("COMPLETED");

        // 关键点：将结束时间修改为当前时间
        // 这样数据库的 conflict 查询 (start < end AND end > start) 就不会再匹配到这个订单
        // 从而立刻释放座位给下一位同学
        r.setEndTime(LocalDateTime.now());

        this.updateById(r);

        // 离座不加分，但也不扣分，是正常流程
        log.info("订单 {} 正常结束使用", reservationId);
    }

    @Override
    public void processNoShows() {
        // 定时任务兜底 (防止 MQ 挂了)
        // 实际场景建议配合 @Scheduled 每分钟跑一次，扫描所有 RESERVED 且超时的订单
    }

    // 升级版：带日志的信用分更新
    private void updateUserCredit(Long userId, int delta, String reason) {
        if (delta == 0) return;

        User user = userMapper.selectById(userId);
        if (user != null) {
            int oldScore = user.getCreditScore();
            int newScore = Math.max(0, Math.min(110, oldScore + delta));

            user.setCreditScore(newScore);
            userMapper.updateById(user);

            // 记录日志
            CreditLog log = new CreditLog();
            log.setUserId(userId);
            log.setType(delta > 0 ? "ADD" : "REDUCE");
            log.setScore(Math.abs(delta));
            log.setReason(reason);
            log.setCreateTime(LocalDateTime.now());

            creditLogMapper.insert(log);
        }
    }
}