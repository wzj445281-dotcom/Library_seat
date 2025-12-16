package com.example.zhizuo.controller;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.service.ReservationService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reservation")
@CrossOrigin(origins = "*") // 允许小程序跨域调用
public class ReservationController {

    private final ReservationService service;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    // 预约接口
    // POST /api/reservation/reserve?userId=1&seatId=1&start=2023-12-01 10:00:00&end=...
    @PostMapping("/reserve")
    public ApiResponse<String> reserve(@RequestParam Long userId,
                                       @RequestParam Long seatId,
                                       @RequestParam String start,
                                       @RequestParam String end) {
        // 解析前端传来的时间字符串
        LocalDateTime startTime = LocalDateTime.parse(start, fmt);
        LocalDateTime endTime = LocalDateTime.parse(end, fmt);

        service.reserve(userId, seatId, startTime, endTime);
        return ApiResponse.success("预约成功");
    }

    // 签到接口
    @PostMapping("/checkin/{id}")
    public ApiResponse<String> checkIn(@PathVariable Long id) {
        service.checkIn(id);
        return ApiResponse.success("签到成功，信用分 +1");
    }

    // 取消接口
    @PostMapping("/cancel/{id}")
    public ApiResponse<String> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ApiResponse.success("取消成功");
    }

    // 离座接口
    @PostMapping("/leave/{id}")
    public ApiResponse<String> leave(@PathVariable Long id) {
        service.leave(id);
        return ApiResponse.success("离座成功");
    }
}