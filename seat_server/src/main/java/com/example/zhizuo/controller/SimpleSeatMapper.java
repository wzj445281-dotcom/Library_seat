package com.example.zhizuo.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 偷懒写法：为了省事，我直接在 Controller 里定义一个内部 Mapper
// 规范做法应该去 mapper 包里建 SeatMapper
@Mapper
interface SimpleSeatMapper extends BaseMapper<Seat> {
    @Select("SELECT * FROM seats") // 确保表名和数据库一致
    List<Seat> selectAll();
}

@RestController
@RequestMapping("/api/seat")
public class SeatController {

    private final SimpleSeatMapper seatMapper;

    public SeatController(SimpleSeatMapper seatMapper) {
        this.seatMapper = seatMapper;
    }

    @GetMapping("/layout")
    public ApiResponse<List<Seat>> getSeatLayout() {
        // 这个接口返回所有座位信息，前端拿到后根据 gridX, gridY 渲染网格
        return ApiResponse.success(seatMapper.selectAll());
    }
}