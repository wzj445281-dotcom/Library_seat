package com.example.zhizuo.api.app;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.RedisUtil;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.mapper.SeatMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/seat")
@Tag(name = "App-座位模块")
public class AppSeatController {

    private final SeatMapper seatMapper;
    private final RedisUtil redisUtil;

    // 定义缓存 Key
    private static final String SEAT_LAYOUT_KEY = "seat:layout:all";

    public AppSeatController(SeatMapper seatMapper, RedisUtil redisUtil) {
        this.seatMapper = seatMapper;
        this.redisUtil = redisUtil;
    }

    @Operation(summary = "获取座位布局(Redis缓存)")
    @GetMapping("/layout")
    public ApiResponse<List<Seat>> getLayout() {
        // 1. 先查 Redis
        if (redisUtil.hasKey(SEAT_LAYOUT_KEY)) {
            // log.info("Hit Redis Cache for Seat Layout");
            List<Seat> cachedSeats = (List<Seat>) redisUtil.get(SEAT_LAYOUT_KEY);
            return ApiResponse.success(cachedSeats);
        }

        // 2. Redis 没命中，查数据库
        List<Seat> seats = seatMapper.selectList(null);

        // 3. 写入 Redis (设置 1 小时过期，防止数据长时间不一致)
        if (seats != null && !seats.isEmpty()) {
            redisUtil.set(SEAT_LAYOUT_KEY, seats, 1, TimeUnit.HOURS);
            // log.info("Cache Miss. Loaded from DB and set to Redis");
        }

        return ApiResponse.success(seats);
    }
}