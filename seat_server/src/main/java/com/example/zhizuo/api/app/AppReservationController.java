package com.example.zhizuo.api.app;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.SecurityUtils;
import com.example.zhizuo.core.dto.ReservationRequestDTO;
import com.example.zhizuo.core.entity.ServiceBooking;
import com.example.zhizuo.core.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app/reservation")
@Tag(name = "App-预约模块")
public class AppReservationController {

    private final ReservationService service;

    // 构造器注入 Service，移除 UserMapper
    public AppReservationController(ReservationService service) {
        this.service = service;
    }

    @Operation(summary = "提交预约")
    @PostMapping("/reserve")
    public ApiResponse<String> reserve(@RequestBody @Validated ReservationRequestDTO requestDTO) {
        // 1. 获取当前用户学号 (一行代码解决)
        String studentId = SecurityUtils.getCurrentStudentId();

        // 2. 调用 Service (直接传 studentId)
        service.reserve(studentId, requestDTO.getSeatId(), requestDTO.getStartTime(), requestDTO.getEndTime());

        return ApiResponse.success("预约成功");
    }

    @Operation(summary = "我的预约列表")
    @GetMapping("/list")
    public ApiResponse<List<ServiceBooking>> getMyList() {
        String studentId = SecurityUtils.getCurrentStudentId();
        return ApiResponse.success(service.getUserReservations(studentId));
    }

    @Operation(summary = "签到")
    @PostMapping("/checkin/{id}")
    public ApiResponse<String> checkIn(@PathVariable Long id) {
        // 异常处理已下沉到 GlobalExceptionHandler
        service.checkIn(id);
        return ApiResponse.success("签到成功");
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel/{id}")
    public ApiResponse<String> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ApiResponse.success("取消成功");
    }

    @Operation(summary = "结束使用(离座)")
    @PostMapping("/leave/{id}")
    public ApiResponse<String> leave(@PathVariable Long id) {
        service.leave(id);
        return ApiResponse.success("离座成功，欢迎下次再来");
    }
}