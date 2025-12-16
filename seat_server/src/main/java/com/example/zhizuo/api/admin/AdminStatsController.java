package com.example.zhizuo.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.SeatMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final UserMapper userMapper;
    private final ReservationMapper reservationMapper;
    private final SeatMapper seatMapper;

    public AdminStatsController(UserMapper userMapper, ReservationMapper reservationMapper, SeatMapper seatMapper) {
        this.userMapper = userMapper;
        this.reservationMapper = reservationMapper;
        this.seatMapper = seatMapper;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 1. 核心指标卡片
        data.put("totalUsers", userMapper.selectCount(null)); // 总用户
        data.put("totalSeats", seatMapper.selectCount(null)); // 总座位

        // 统计今日预约数 (start_time 在今天 00:00 到 23:59 之间)
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        QueryWrapper<Reservation> todayQuery = new QueryWrapper<>();
        todayQuery.between("start_time", todayStart, todayEnd);
        data.put("todayOrders", reservationMapper.selectCount(todayQuery));

        // 统计高风险用户 (信用分 < 60)
        QueryWrapper<User> riskQuery = new QueryWrapper<>();
        riskQuery.lt("credit_score", 60);
        data.put("riskyUsers", userMapper.selectCount(riskQuery));

        // 2. 简单的图表数据 (模拟)
        // 比如：最近7天的预约趋势，这里为了演示先写死，后期可以用 SQL 统计
        int[] trendData = {120, 132, 101, 134, 90, 230, 210};
        data.put("weeklyTrend", trendData);

        return ApiResponse.success(data);
    }
}