package com.petsaas.controller.merchant;

import com.petsaas.common.ApiResponse;
import com.petsaas.service.MerchantOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/orders")
@Tag(name = "Merchant-订单管理")
public class MerchantOrderController {

    @Autowired
    private MerchantOrderService merchantOrderService;

    @Operation(summary = "订单核销(扫码)")
    @PostMapping("/verify")
    public ApiResponse verifyOrder(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo"); // 或者是核销码
        if (orderNo == null) return ApiResponse.error("订单号不能为空");

        try {
            boolean success = merchantOrderService.verifyOrder(orderNo);
            if (success) {
                return ApiResponse.success("核销成功！订单已完成");
            } else {
                return ApiResponse.error("核销失败，订单状态无效或已使用");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "查询今日待核销订单")
    @GetMapping("/pending")
    public ApiResponse getPendingOrders() {
        // 这里假设所有商户共用逻辑，实际应根据 merchantId 过滤
        return ApiResponse.success(merchantOrderService.getPendingOrders());
    }
}