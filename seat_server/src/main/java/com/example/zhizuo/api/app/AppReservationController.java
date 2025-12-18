package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.dto.ReservationRequestDTO;
import com.example.zhizuo.core.entity.Reservation;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app/reservation")
@Tag(name = "App-预约模块")
public class AppReservationController {

    private final ReservationService service;
    private final UserMapper userMapper;

    public AppReservationController(ReservationService service, UserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
    }

    @Operation(summary = "提交预约")
    @PostMapping("/reserve")
    public ApiResponse<String> reserve(@RequestBody @Validated ReservationRequestDTO requestDTO) {
        // 1. 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = (String) auth.getPrincipal();

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        User user = userMapper.selectOne(query);
        if (user == null) return ApiResponse.error(401, "用户不存在");

        service.reserve(user.getId(), requestDTO.getSeatId(), requestDTO.getStartTime(), requestDTO.getEndTime());
        return ApiResponse.success("预约成功");
    }

    @Operation(summary = "我的预约列表")
    @GetMapping("/list")
    public ApiResponse<List<Reservation>> getMyList() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = (String) auth.getPrincipal();
        return ApiResponse.success(service.getUserReservations(studentId));
    }

    @Operation(summary = "签到")
    @PostMapping("/checkin/{id}")
    public ApiResponse<String> checkIn(@PathVariable Long id) {
        try {
            service.checkIn(id);
            return ApiResponse.success("签到成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel/{id}")
    public ApiResponse<String> cancel(@PathVariable Long id) {
        try {
            service.cancel(id);
            return ApiResponse.success("取消成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // --- 新增：结束使用/离座 ---
    @Operation(summary = "结束使用(离座)")
    @PostMapping("/leave/{id}")
    public ApiResponse<String> leave(@PathVariable Long id) {
        try {
            service.leave(id);
            return ApiResponse.success("离座成功，欢迎下次再来");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}