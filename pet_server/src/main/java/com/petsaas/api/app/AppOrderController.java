package com.petsaas.api.app;

import com.petsaas.api.Result;
import com.petsaas.common.util.SecurityUtils;
import com.petsaas.core.dto.OrderCreateDTO;
import com.petsaas.core.entity.ProductOrder;
import com.petsaas.core.service.ProductOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app/order")
@Tag(name = "App-订单交易")
public class AppOrderController {

    @Autowired
    private ProductOrderService productOrderService;

    @Operation(summary = "创建商品订单")
    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody OrderCreateDTO dto) {
        Long userId = SecurityUtils.getUserId();
        String orderNo = productOrderService.createOrder(userId, dto);
        return Result.success(orderNo);
    }

    @Operation(summary = "获取我的订单列表")
    @GetMapping("/list")
    public Result<List<ProductOrder>> getMyOrders(@RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getUserId();
        return Result.success(productOrderService.getUserOrders(userId, status));
    }

    @Operation(summary = "模拟支付")
    @PostMapping("/pay/{orderId}")
    public Result<Boolean> payOrder(@PathVariable Long orderId) {
        boolean success = productOrderService.payOrder(orderId);
        return success ? Result.success(true) : Result.error("支付失败或订单状态异常");
    }
}