package com.example.zhizuo.controller;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.entity.Seat;
import com.example.zhizuo.mapper.SeatMapper; // 使用正确的 Mapper
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seat")
public class SeatController {

    private final SeatMapper seatMapper;

    public SeatController(SeatMapper seatMapper) {
        this.seatMapper = seatMapper;
    }

    @GetMapping("/layout")
    public ApiResponse<List<Seat>> getSeatLayout() {
        // 使用 MyBatis Plus 自带的 selectList(null) 查询所有
        return ApiResponse.success(seatMapper.selectList(null));
    }
}