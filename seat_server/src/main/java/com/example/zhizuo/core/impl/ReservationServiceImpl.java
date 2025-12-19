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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预约服务核心实现类 (V2.0 Enterprise Edition)
 * <p>
 * 包含：
 * 1. Redisson 分布式锁防超卖
 * 2. AI 风控拦截 (Python集成)
 * 3. 信用分自动恢复引擎
 * 4. 权益策略动态计算 (Redis)
 * 5. 双重兜底机制 (MQ延迟队列 + 定时任务扫描)
 */
@Slf4j
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final UserMapper userMapper;
    private final RedissonClient redissonClient;
    private final ReservationProducer reservationProducer;
    private final AiRiskService aiRiskService;
    private final CreditLogMapper creditLogMapper;
    private final ReservationMapper reservationMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ReservationServiceImpl(UserMapper userMapper,
                                  RedissonClient redissonClient,
                                  ReservationProducer reservationProducer,
                                  AiRiskService aiRiskService,
                                  CreditLogMapper creditLogMapper,
                                  ReservationMapper reservationMapper,
                                  RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redissonClient = redissonClient;
        this.reservationProducer = reservationProducer;
        this.aiRiskService = aiRiskService;
        this.creditLogMapper = creditLogMapper;
        this.reservationMapper = reservationMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取用户历史预约记录
     *
     * @param studentId 学号
     * @return 预约列表
     */
    @Override
    public List<Reservation> getUserReservations(String studentId) {
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("student_id", studentId);
        User user = userMapper.selectOne(userQuery);

        if (user == null) {
            log.warn("查询预约记录失败，学号 {} 不存在", studentId);
            return Collections.emptyList();
        }

        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("user_id", user.getId());
        // 按创建时间倒序，优先展示最近的预约
        query.orderByDesc("create_time");
        return this.list(query);
    }

    /**
     * 核心预约逻辑
     * 采用 "校验 -> 风控 -> 锁 -> 交易 -> 消息" 的标准处理流程
     *
     * @param userId 用户ID
     * @param seatId 座位ID
     * @param start  开始时间
     * @param end    结束时间
     */
    @Override
    public void reserve(Long userId, Long seatId, LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 基础参数校验
        if (start.isBefore(now)) {
            throw new RuntimeException("无法预约过去的时间");
        }
        if (end.isBefore(start)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }

        // --- 核心修改：动态权益计算 (Strategy Implementation) ---
        long maxHours = 4; // 基础时长限制
        // Key 格式: user:benefit:time_extend:{userId}
        String benefitKey = "user:benefit:time_extend:" + userId;
        Object extendObj = redisTemplate.opsForValue().get(benefitKey);

        if (extendObj != null) {
            try {
                int extendHours = Integer.parseInt(extendObj.toString());
                maxHours += extendHours;
                log.info("用户 {} 触发加时权益，增加 {} 小时，当前上限 {} 小时", userId, extendHours, maxHours);
            } catch (NumberFormatException e) {
                log.error("权益数据解析异常 userId: {}", userId, e);
            }
        }

        long requestHours = Duration.between(start, end).toHours();
        if (requestHours > maxHours) {
            throw new RuntimeException(String.format("当前预约时长(%d小时)超过您的权限上限(%d小时)，请前往积分商城兑换加时卡", requestHours, maxHours));
        }
        // --- 权益计算结束 ---

        // 2. 用户状态校验
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("非法用户");
        }
        // 黑名单熔断机制
        if (user.getCreditScore() < 60) {
            throw new RuntimeException("信用分低于60，系统已自动暂停您的预约权限，请联系管理员。");
        }

        // 3. AI 智能风控拦截
        // 调用 Python AI 微服务，基于用户历史行为序列进行风险预测
        boolean isHighRisk = aiRiskService.isHighRiskUser(user.getCreditScore());
        if (isHighRisk) {
            log.warn("AI 风控警报：用户 {} 存在高风险行为特征", userId);
            // 降级策略：高风险用户限制单次预约时长不超过 2 小时
            if (requestHours > 2) {
                throw new RuntimeException("系统风控提示：检测到您的账号近期存在异常或信用波动，为保障公共资源公平，暂时限制单次预约时长为2小时。");
            }
        }

        // 4. Redisson 分布式锁 (防并发超卖)
        // 锁粒度：细化到单个座位
        String lockKey = "seat_lock:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁：等待 5s，持锁 10s (防止死锁)
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (isLocked) {
                try {
                    // --- 临界区 (Critical Section) ---

                    // 双重检查 (Double Check)：数据库层面查询时间段冲突
                    // SQL: SELECT count(*) FROM reservation WHERE seat_id=? AND status IN (...) AND NOT (end_time <= ? OR start_time >= ?)
                    int conflictCount = reservationMapper.countConflict(seatId, start, end);
                    if (conflictCount > 0) {
                        log.info("座位 {} 在时段 {} - {} 已被占用", seatId, start, end);
                        throw new RuntimeException("手慢了！该时段座位已被其他同学抢订");
                    }

                    // 组装订单对象
                    Reservation reservation = new Reservation();
                    reservation.setUserId(userId);
                    reservation.setSeatId(seatId);
                    reservation.setStartTime(start);
                    reservation.setEndTime(end);
                    reservation.setStatus("RESERVED"); // 初始状态：已预约
                    reservation.setCreateTime(now);

                    // 落库
                    this.save(reservation);

                    // 5. 消息队列异步处理
                    // 发送 RabbitMQ 延迟消息 (15分钟后检查是否签到)
                    reservationProducer.sendDelayMessage(reservation.getId());

                    log.info("用户 {} 预约座位 {} 成功，订单ID: {}", userId, seatId, reservation.getId());
                    // --- 临界区结束 ---

                } finally {
                    // 释放锁
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new RuntimeException("当前抢座人数过多，系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
            throw new RuntimeException("系统内部错误");
        }
    }

    /**
     * 用户签到
     *
     * @param reservationId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null) {
            throw new RuntimeException("预约订单不存在");
        }

        if (!"RESERVED".equals(r.getStatus())) {
            throw new RuntimeException("订单当前状态[" + r.getStatus() + "]不支持签到");
        }

        LocalDateTime now = LocalDateTime.now();
        // 规则：允许开始前 15 分钟内签到
        LocalDateTime allowCheckInTime = r.getStartTime().minusMinutes(15);

        if (now.isBefore(allowCheckInTime)) {
            throw new RuntimeException("未到签到时间，请在预约开始前15分钟内签到");
        }

        // 规则：如果已经超过开始时间 15 分钟，则视为违约 (通常由MQ处理，但此处做双重校验)
        if (now.isAfter(r.getStartTime().plusMinutes(15))) {
            throw new RuntimeException("签到超时，系统已自动取消该预约并记录违约");
        }

        // 更新状态
        r.setStatus("CHECKED_IN");
        r.setCheckInTime(now);
        this.updateById(r);

        // 正向激励：准时签到 +1 分
        updateUserCredit(r.getUserId(), 1, "预约履约-准时签到");
        log.info("订单 {} 签到成功", reservationId);
    }

    /**
     * 用户取消预约
     *
     * @param reservationId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!"RESERVED".equals(r.getStatus())) {
            throw new RuntimeException("只能取消[已预约]状态的订单");
        }

        // 更新状态
        r.setStatus("CANCELLED");
        this.updateById(r);

        // 惩罚机制：临期取消 (距离开始不足30分钟) 扣分
        long minutesToStart = Duration.between(LocalDateTime.now(), r.getStartTime()).toMinutes();
        if (minutesToStart < 30 && minutesToStart >= -15) { // 兼容稍微过时一点点的边界
            updateUserCredit(r.getUserId(), -2, "临期取消预约(不足30分钟)");
            log.info("用户 {} 临期取消订单 {}，触发扣分", r.getUserId(), reservationId);
        } else {
            log.info("订单 {} 已无责取消", reservationId);
        }
    }

    /**
     * 用户离座 (结束使用)
     *
     * @param reservationId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leave(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null) throw new RuntimeException("订单不存在");

        if (!"CHECKED_IN".equals(r.getStatus())) {
            throw new RuntimeException("您尚未签到，无法执行正常离座操作");
        }

        // 更新状态
        r.setStatus("COMPLETED");
        r.setEndTime(LocalDateTime.now()); // 释放资源：结束时间更新为当前实际离开时间
        this.updateById(r);

        log.info("订单 {} 正常结束使用", reservationId);

        // 信用恢复引擎 (Credit Recovery Engine)
        // 触发条件：用户完成了一次完美的闭环 (预约->签到->离座)
        checkAndRecoverCredit(r.getUserId());
    }

    /**
     * 信用恢复引擎核心逻辑
     * 检查用户近期的履约表现，如果表现良好，额外奖励积分
     */
    private void checkAndRecoverCredit(Long userId) {
        // 获取最近 3 次记录
        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.orderByDesc("create_time");
        query.last("LIMIT 3");
        List<Reservation> history = this.list(query);

        // 数据不足不处理
        if (history.size() < 3) return;

        // 检查是否连续 3 次状态都是 COMPLETED
        boolean allPerfect = history.stream()
                .allMatch(res -> "COMPLETED".equals(res.getStatus()));

        if (allPerfect) {
            // 这里可以加一个 Redis 锁或者标记，防止重复奖励同一批次
            // 简单实现：直接调用加分，由 updateUserCredit 的上限逻辑控制
            updateUserCredit(userId, 2, "连续3次完美履约奖励");
            log.info("用户 {} 触发信用恢复引擎，获得额外奖励", userId);
        }
    }

    /**
     * 处理违约 (MQ 消费者调用此方法)
     * 逻辑：如果到了指定时间（开始后15分钟）用户仍未签到，则视为违约
     */
    public void handleNoShow(Long reservationId) {
        RLock lock = redissonClient.getLock("res_handle:" + reservationId);
        try {
            if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                try {
                    Reservation r = this.getById(reservationId);
                    if (r == null) return;

                    // 只有在 RESERVED 状态下才处理
                    // 如果已经是 CHECKED_IN (已签到) 或 CANCELLED (已取消)，则忽略
                    if ("RESERVED".equals(r.getStatus())) {
                        LocalDateTime deadline = r.getStartTime().plusMinutes(15);
                        if (LocalDateTime.now().isAfter(deadline)) {
                            // 标记为违约
                            r.setStatus("VIOLATION");
                            this.updateById(r);

                            // 扣分：违约扣 5 分
                            updateUserCredit(r.getUserId(), -5, "预约违约-未签到");
                            log.warn("订单 {} 确认违约，已执行扣分", reservationId);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("违约处理加锁失败", e);
        }
    }

    /**
     * 兜底定时任务：处理系统遗漏的违约单
     * <p>
     * 场景：MQ 服务抖动或消息丢失，导致部分订单长期卡在 RESERVED 状态占用库存。
     * 建议配置 @Scheduled(cron = "0 0/30 * * * ?") 每半小时执行一次
     */
    @Override
    public void processNoShows() {
        log.info("开始执行违约订单兜底扫描...");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(20); // 超过开始时间20分钟仍未签到

        // 查询条件：status = RESERVED AND start_time < (now - 20min)
        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("status", "RESERVED");
        query.lt("start_time", threshold);
        // 限制每次处理条数，防止大事务
        query.last("LIMIT 100");

        List<Reservation> stuckReservations = this.list(query);
        if (stuckReservations.isEmpty()) {
            return;
        }

        for (Reservation r : stuckReservations) {
            try {
                // 复用 handleNoShow 逻辑
                this.handleNoShow(r.getId());
            } catch (Exception e) {
                log.error("处理兜底违约订单 {} 失败", r.getId(), e);
            }
        }
        log.info("兜底扫描结束，处理了 {} 条异常订单", stuckReservations.size());
    }

    /**
     * 统一信用分更新方法 (包含边界检查和日志记录)
     *
     * @param userId 用户ID
     * @param delta  变动分值 (正数为加分，负数为扣分)
     * @param reason 变动原因
     */
    private void updateUserCredit(Long userId, int delta, String reason) {
        if (delta == 0) return;

        User user = userMapper.selectById(userId);
        if (user != null) {
            int oldScore = user.getCreditScore();

            // 规则：满分 110 分，超过不加
            if (delta > 0 && oldScore >= 110) {
                log.info("用户 {} 信用分已满，本次不再增加", userId);
                return;
            }
            // 规则：最低 0 分
            int newScore = oldScore + delta;
            if (newScore < 0) newScore = 0;
            if (newScore > 110) newScore = 110;

            if (newScore != oldScore) {
                user.setCreditScore(newScore);
                userMapper.updateById(user);

                // 记录流水日志
                CreditLog logEntry = new CreditLog();
                logEntry.setUserId(userId);
                logEntry.setType(delta > 0 ? "ADD" : "REDUCE");
                logEntry.setScore(Math.abs(delta));
                logEntry.setReason(reason);
                logEntry.setCreateTime(LocalDateTime.now());

                creditLogMapper.insert(logEntry);
                log.info("用户 {} 信用分变更: {} -> {} (原因: {})", userId, oldScore, newScore, reason);
            }
        }
    }
}