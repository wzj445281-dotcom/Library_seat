package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.RedisUtil;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.entity.SeatHeatStats;
import com.example.zhizuo.core.mapper.SeatHeatStatsMapper;
import com.example.zhizuo.core.mapper.SeatMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/seat")
@Tag(name = "App-座位模块")
public class AppSeatController {

    private final SeatMapper seatMapper;
    private final SeatHeatStatsMapper seatHeatStatsMapper; // 新增注入
    private final RedisUtil redisUtil;

    private static final String SEAT_LAYOUT_KEY = "seat:layout:v2"; // 升级 Key 版本

    public AppSeatController(SeatMapper seatMapper, SeatHeatStatsMapper seatHeatStatsMapper, RedisUtil redisUtil) {
        this.seatMapper = seatMapper;
        this.seatHeatStatsMapper = seatHeatStatsMapper;
        this.redisUtil = redisUtil;
    }

    @Operation(summary = "获取座位布局(含AI热度)")
    @GetMapping("/layout")
    public ApiResponse<List<Seat>> getLayout() {
        // 1. 尝试从 Redis 拿缓存
        if (redisUtil.hasKey(SEAT_LAYOUT_KEY)) {
            @SuppressWarnings("unchecked")
            List<Seat> cachedSeats = (List<Seat>) redisUtil.get(SEAT_LAYOUT_KEY);
            return ApiResponse.success(cachedSeats);
        }

        // 2. 查座位基础信息
        List<Seat> seats = seatMapper.selectList(null);

        // 3. 查明天的预测热度数据
        QueryWrapper<SeatHeatStats> query = new QueryWrapper<>();
        query.eq("prediction_date", LocalDate.now().plusDays(1));
        List<SeatHeatStats> statsList = seatHeatStatsMapper.selectList(query);

        // 4. 数据组装 (Map 优化性能)
        if (statsList != null && !statsList.isEmpty()) {
            Map<Long, Double> scoreMap = statsList.stream()
                    .collect(Collectors.toMap(SeatHeatStats::getSeatId, SeatHeatStats::getHeatScore, (k1, k2) -> k1));

            for (Seat seat : seats) {
                // 如果有预测分就填入，没有就默认 50 (中等热度)
                seat.setHeatScore(scoreMap.getOrDefault(seat.getId(), 50.0));
            }
        } else {
            // 如果还没跑 Python 脚本，给个默认值防止前端报错
            seats.forEach(s -> s.setHeatScore(0.0));
        }

        // 5. 写入 Redis (缓存 30 分钟)
        redisUtil.set(SEAT_LAYOUT_KEY, seats, 30, TimeUnit.MINUTES);

        return ApiResponse.success(seats);
    }
}