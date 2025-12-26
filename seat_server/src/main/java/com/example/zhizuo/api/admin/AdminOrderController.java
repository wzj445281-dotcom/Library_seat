package com.example.zhizuo.api.admin;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@Tag(name = "Admin-订单管理")
public class AdminOrderController {

    @Resource
    private OrderService orderService;

    @Operation(summary = "商家看板订单列表")
    @GetMapping("/list")
    public ApiResponse<List<Order>> list(@RequestParam(required = false, defaultValue = "PAID") String status) {
        // 返回 List<Order> 供前端表格显示
        return ApiResponse.success(orderService.getAdminOrderList(status));
    }

    @Operation(summary = "更新订单状态")
    @PostMapping("/status")
    public ApiResponse<String> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String status = (String) params.get("status");

        try {
            orderService.updateOrderStatus(id, status);
            return ApiResponse.success("状态更新成功");
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}