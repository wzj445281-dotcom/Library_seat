package com.example.zhizuo.api.app;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.SecurityUtils;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端订单接口
 */
@RestController
@RequestMapping("/app/orders")
@Tag(name = "C端-订单管理")
public class AppOrderController {

    @Autowired
    private OrderService orderService;

    // ✅【关键修复】路径改为 /list，参数改为 String status
    @GetMapping("/list")
    @Operation(summary = "获取我的订单列表")
    public ApiResponse<List<Order>> getMyOrderList(@RequestParam(required = false) String status) {

        Long userId = SecurityUtils.getUserId();
        // 调用 Service 层处理逻辑
        List<Order> list = orderService.getUserOrderList(userId, status);

        return ApiResponse.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public ApiResponse<Order> getOrderDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        Order order = orderService.getDetailWithItems(id);

        if (order == null) return ApiResponse.error(404, "订单不存在");
        if (!order.getUserId().equals(userId)) return ApiResponse.error(403, "无权访问该订单");

        return ApiResponse.success(order);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public ApiResponse<Boolean> cancelOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        Order order = orderService.getById(id);

        if (order == null || !order.getUserId().equals(userId)) {
            return ApiResponse.error(404, "订单不存在或无权操作");
        }

        // 简单逻辑：取消订单
        orderService.closeOrderAndRestoreStock(id, "用户主动取消");
        return ApiResponse.success(true);
    }
}