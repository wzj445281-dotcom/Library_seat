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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
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
        // ... existing code ...
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
        int[] trendData = {120, 132, 101, 134, 90, 230, 210};
        data.put("weeklyTrend", trendData);

        return ApiResponse.success(data);
    }

    // 新增：导出 Excel 接口
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        // 1. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("预约记录表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 2. 查询数据 (演示查最近 100 条)
        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        query.last("LIMIT 100");
        List<Reservation> list = reservationMapper.selectList(query);

        // 3. 填充关联数据 (User, Seat)
        // 为了性能，应该先把 ID 收集起来批量查，这里数据量小直接循环查演示逻辑
        List<ReservationExportVO> exportData = new ArrayList<>();

        // 预加载所有 Seat 和 User 缓存 (简单优化)
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

        // 4. 写出 Excel
        EasyExcel.write(response.getOutputStream(), ReservationExportVO.class)
                .sheet("预约明细")
                .doWrite(exportData);
    }
}