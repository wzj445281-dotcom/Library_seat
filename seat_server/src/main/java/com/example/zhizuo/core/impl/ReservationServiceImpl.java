package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j // 需要引入 lombok 或者手动使用 LoggerFactory
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final UserMapper userMapper;
    // baseMapper 已经在父类 ServiceImpl 中注入了，直接使用即可，也可以显式声明 ReservationMapper

    public ReservationServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<Reservation> getUserReservations(String studentId) {
        // 1. 先查用户ID
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("student_id", studentId);
        User user = userMapper.selectOne(userQuery);

        // 如果用户不存在，返回空列表
        if (user == null) {
            return Collections.emptyList();
        }

        // 2. 根据用户ID查预约记录，按创建时间倒序排列
        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("user_id", user.getId());
        query.orderByDesc("create_time"); // 最新预约在最上面

        return this.list(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(Long userId, Long seatId, LocalDateTime start, LocalDateTime end) {
        // 1. 基础参数校验
        if (start == null || end == null) {
            throw new IllegalArgumentException("时间不能为空");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("不能预约过去的时间");
        }
        if (end.isBefore(start)) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }

        // 限制最大预约时长 (例如4小时) - 增加业务逻辑
        long hours = Duration.between(start, end).toHours();
        if (hours > 4) {
            throw new RuntimeException("单次预约不能超过 4 小时");
        }

        // 2. 信用分门槛校验
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        // 规则：信用分 < 60 禁止预约
        if (user.getCreditScore() < 60) {
            throw new RuntimeException("您的信用分(" + user.getCreditScore() + ")过低，已被限制预约，请联系管理员申诉");
        }

        // 3. 冲突检测 (核心算法)
        // 使用 baseMapper (即 ReservationMapper) 的自定义 SQL
        int conflictCount = baseMapper.countConflict(seatId, start, end);
        if (conflictCount > 0) {
            throw new RuntimeException("该时段座位已被他人抢订");
        }

        // 4. 执行预约
        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setSeatId(seatId);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setStatus("RESERVED");
        // baseMapper.insert(r); 或者 this.save(r);
        this.save(r);

        log.info("用户 {} 成功预约座位 {}", userId, seatId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null) throw new RuntimeException("预约记录不存在");

        // 状态校验
        if (!"RESERVED".equals(r.getStatus())) {
            throw new RuntimeException("当前状态不可签到 (需为已预约状态)");
        }

        // 时间窗口校验 (高级逻辑)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = r.getStartTime();
        // 规则：允许提前 15 分钟签到
        if (now.isBefore(start.minusMinutes(15))) {
            throw new RuntimeException("未到签到时间，请于开始前 15 分钟内签到");
        }
        // 规则：迟到 15 分钟以上算违约 (在定时任务里处理，这里主要是防止系统还没跑定时任务用户就来签到)
        if (now.isAfter(start.plusMinutes(15))) {
            // 实际上这应该由定时任务处理成 VIOLATION，但如果定时任务有延迟，这里可以补刀
            throw new RuntimeException("已超过签到时间，视为违约");
        }

        // 更新状态
        r.setStatus("CHECKED_IN");
        r.setCheckInTime(now);
        this.updateById(r);

        // 信用分奖励 (+1分)
        updateUserCredit(r.getUserId(), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null) throw new RuntimeException("预约记录不存在");

        if (!"RESERVED".equals(r.getStatus())) {
            throw new RuntimeException("只能取消[已预约]状态的订单");
        }

        // 更新状态
        r.setStatus("CANCELLED");
        this.updateById(r);

        // 判定是否“临时取消” (距离开始不足 30 分钟)
        long minutesUntilStart = Duration.between(LocalDateTime.now(), r.getStartTime()).toMinutes();
        if (minutesUntilStart < 30 && minutesUntilStart >= 0) {
            // 临时取消扣 2 分
            updateUserCredit(r.getUserId(), -2);
            log.info("用户 {} 临时取消预约，扣除信用分", r.getUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leave(Long reservationId) {
        Reservation r = this.getById(reservationId);
        if (r == null) throw new RuntimeException("预约记录不存在");

        if (!"CHECKED_IN".equals(r.getStatus())) {
            throw new RuntimeException("您尚未签到，无法离座");
        }

        r.setStatus("COMPLETED");
        r.setEndTime(LocalDateTime.now()); // 实际结束时间更新为当前时间，释放座位资源
        this.updateById(r);

        // 这里可以扩展：如果实际坐够了时长，额外奖励积分等
    }

    /**
     * 定时任务：每分钟检查一次违约
     */
    @Override
    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void processNoShows() {
        // 截止时间 = 当前时间 - 15分钟
        // 即：如果现在是 10:16，那么 10:01 之前开始且还没签到的订单，都算违约
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(15);

        // 查询条件：状态=RESERVED 且 开始时间 < deadline
        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("status", "RESERVED")
                .lt("start_time", deadline);

        List<Reservation> expiredList = this.list(query);

        if (!expiredList.isEmpty()) {
            log.info("定时任务检测到 {} 个违约订单", expiredList.size());
            for (Reservation r : expiredList) {
                // 1. 标记违约
                r.setStatus("VIOLATION");
                this.updateById(r);

                // 2. 扣除信用分 (-5分)
                updateUserCredit(r.getUserId(), -5);
            }
        }
    }

    /**
     * 私有辅助方法: 更新信用分
     * 封装了加减分逻辑和上下限控制
     */
    private void updateUserCredit(Long userId, int delta) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            int oldScore = user.getCreditScore();
            int newScore = oldScore + delta;

            // 规则：上限 110 分
            if (newScore > 110) newScore = 110;
            // 规则：下限 0 分
            if (newScore < 0) newScore = 0;

            if (oldScore != newScore) {
                user.setCreditScore(newScore);
                userMapper.updateById(user);
                // 💡 未来升级点：这里插入 CreditLog 表记录流水
            }
        }
    }
}