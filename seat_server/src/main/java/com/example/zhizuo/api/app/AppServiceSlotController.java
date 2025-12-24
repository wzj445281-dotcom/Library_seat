package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.RedisUtil;
import com.example.zhizuo.core.entity.ServiceSlot;
import com.example.zhizuo.core.mapper.ServiceSlotMapper;
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
@RequestMapping("/api/service-slot")
@Tag(name = "App-服务工位模块")
public class AppServiceSlotController {

    private final ServiceSlotMapper serviceSlotMapper;
    private final RedisUtil redisUtil;

    private static final String SERVICE_SLOT_LAYOUT_KEY = "service_slot:layout:v1"; // 缓存Key

    public AppServiceSlotController(ServiceSlotMapper serviceSlotMapper, RedisUtil redisUtil) {
        this.serviceSlotMapper = serviceSlotMapper;
        this.redisUtil = redisUtil;
    }

    @Operation(summary = "获取服务工位布局")
    @GetMapping("/layout")
    public ApiResponse<List<ServiceSlot>> getLayout() {
        // 1. 尝试从 Redis 拿缓存
        if (redisUtil.hasKey(SERVICE_SLOT_LAYOUT_KEY)) {
            List<ServiceSlot> cachedSlots = (List<ServiceSlot>) redisUtil.get(SERVICE_SLOT_LAYOUT_KEY);
            return ApiResponse.success(cachedSlots);
        }

        // 2. 查工位基础信息
        List<ServiceSlot> slots = serviceSlotMapper.selectList(null);

        // 3. 默认繁忙度值 (后续可以集成AI预测)
        slots.forEach(s -> s.setBusyScore(50.0));

        // 4. 写入 Redis (缓存 30 分钟)
        redisUtil.set(SERVICE_SLOT_LAYOUT_KEY, slots, 30, TimeUnit.MINUTES);

        return ApiResponse.success(slots);
    }
}