package com.example.zhizuo.api.admin;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.SeatMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.vo.ReservationExportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/stats")
@Tag(name = "Admin-数据统计")
public class AdminStatsController {

    private final UserMapper userMapper;
    private final ReservationMapper reservationMapper;
    private final SeatMapper seatMapper;

    public AdminStatsController(UserMapper userMapper, ReservationMapper reservationMapper, SeatMapper seatMapper) {
        this.userMapper = userMapper;
        this.reservationMapper = reservationMapper;
        this.seatMapper = seatMapper;
    }

    @Operation(summary = "获取基础仪表盘数据")
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 1. 基础计数
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalSeats", seatMapper.selectCount(null));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        QueryWrapper<Reservation> todayQuery = new QueryWrapper<>();
        todayQuery.between("start_time", todayStart, todayEnd);
        data.put("todayOrders", reservationMapper.selectCount(todayQuery));

        QueryWrapper<User> riskQuery = new QueryWrapper<>();
        riskQuery.lt("credit_score", 60);
        data.put("riskyUsers", userMapper.selectCount(riskQuery));

        // 模拟周趋势数据
        int[] trendData = {120, 132, 101, 134, 90, 230, 210};
        data.put("weeklyTrend", trendData);

        return ApiResponse.success(data);
    }

    @Operation(summary = "获取高级分析数据 (Task C)")
    @GetMapping("/advanced")
    public ApiResponse<Map<String, Object>> getAdvancedStats() {
        Map<String, Object> data = new HashMap<>();

        // 1. 各时段座位占用率预测 (模拟数据 + 简单计算)
        // 真实场景可以结合 Reservation 表统计未来几个小时的预订量 / 总座位数
        Map<String, Double> occupancyRate = new HashMap<>();
        occupancyRate.put("08:00-10:00", 0.85);
        occupancyRate.put("10:00-12:00", 0.95);
        occupancyRate.put("12:00-14:00", 0.40);
        occupancyRate.put("14:00-16:00", 0.90);
        occupancyRate.put("16:00-18:00", 0.75);
        occupancyRate.put("18:00-22:00", 0.60);
        data.put("occupancyForecast", occupancyRate);

        // 2. 用户平均签到延迟时间 (Avg Check-in Delay)
        // 查最近100条已签到的记录
        QueryWrapper<Reservation> checkInQuery = new QueryWrapper<>();
        checkInQuery.eq("status", "CHECKED_IN");
        checkInQuery.isNotNull("check_in_time");
        checkInQuery.orderByDesc("create_time");
        checkInQuery.last("LIMIT 100");
        List<Reservation> checkedList = reservationMapper.selectList(checkInQuery);

        double avgDelayMinutes = 0.0;
        if (!checkedList.isEmpty()) {
            long totalDelay = 0;
            for (Reservation r : checkedList) {
                // 如果签到时间晚于开始时间，计算延迟
                if (r.getCheckInTime().isAfter(r.getStartTime())) {
                    totalDelay += Duration.between(r.getStartTime(), r.getCheckInTime()).toMinutes();
                } else {
                    // 提前签到算0延迟
                }
            }
            avgDelayMinutes = (double) totalDelay / checkedList.size();
        }
        data.put("avgCheckInDelayMinutes", String.format("%.1f", avgDelayMinutes));

        return ApiResponse.success(data);
    }

    @Operation(summary = "导出报表")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("预约记录表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        query.last("LIMIT 1000");
        List<Reservation> list = reservationMapper.selectList(query);

        List<ReservationExportVO> exportData = new ArrayList<>();
        Map<Long, Seat> seatMap = seatMapper.selectList(null).stream().collect(Collectors.toMap(Seat::getId, s -> s));
        Map<Long, User> userMap = userMapper.selectList(null).stream().collect(Collectors.toMap(User::getId, u -> u));

        for (Reservation r : list) {
            ReservationExportVO vo = new ReservationExportVO();
            vo.setId(r.getId());
            vo.setStartTime(r.getStartTime());
            vo.setEndTime(r.getEndTime());
            vo.setStatus(r.getStatus());
            vo.setCreateTime(r.getCreateTime());

            User u = userMap.get(r.getUserId());
            if (u != null) {
                vo.setStudentId(u.getStudentId());
                vo.setUserName(u.getName());
            }
            Seat s = seatMap.get(r.getSeatId());
            if (s != null) {
                vo.setSeatLabel(s.getLabel());
            }
            exportData.add(vo);
        }

        EasyExcel.write(response.getOutputStream(), ReservationExportVO.class)
                .sheet("预约明细")
                .doWrite(exportData);
    }
}