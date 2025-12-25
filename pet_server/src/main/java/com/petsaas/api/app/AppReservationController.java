package com.petsaas.api.app;

import com.petsaas.api.Result;
import com.petsaas.common.util.SecurityUtils;
import com.petsaas.core.dto.ReservationRequestDTO;
import com.petsaas.core.entity.Reservation;
import com.petsaas.core.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app/reservation")
@Tag(name = "App-预约管理")
public class AppReservationController {

    @Autowired
    private ReservationService reservationService;

    @Operation(summary = "提交预约")
    @PostMapping("/create")
    public Result<Boolean> createReservation(@RequestBody ReservationRequestDTO dto) {
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        boolean success = reservationService.createReservation(userId, dto);
        return success ? Result.success(true) : Result.error("预约失败，该时段可能已被占用");
    }

    @Operation(summary = "获取我的预约列表")
    @GetMapping("/my")
    public Result<List<Reservation>> getMyReservations() {
        Long userId = SecurityUtils.getUserId();
        return Result.success(reservationService.getUserReservations(userId));
    }
}