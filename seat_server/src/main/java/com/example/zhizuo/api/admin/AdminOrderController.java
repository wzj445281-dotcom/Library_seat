package com.example.zhizuo.api.admin;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@Tag(name = "Admin-订单管理(O2O)")
public class AdminOrderController {

    @Resource
    private OrderService orderService;

    @Operation(summary = "商家看板订单列表")
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam(required = false, defaultValue = "PAID") String status) {
        // status: PAID=制作中, READY=待取, ALL=全部
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