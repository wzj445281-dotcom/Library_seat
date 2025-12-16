package com.example.zhizuo.api.app;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.mapper.SeatMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 专门给小程序用的座位接口
 */
@RestController
@RequestMapping("/api/seat") // ⚠️注意：为了兼容前端代码，这里路径保持 /api/seat，不要加 /app
public class AppSeatController {

    private final SeatMapper seatMapper;

    public AppSeatController(SeatMapper seatMapper) {
        this.seatMapper = seatMapper;
    }

    @GetMapping("/layout")
    public ApiResponse<List<Seat>> getLayout() {
        // 查询所有座位，前端自己去画网格
        return ApiResponse.success(seatMapper.selectList(null));
    }
}