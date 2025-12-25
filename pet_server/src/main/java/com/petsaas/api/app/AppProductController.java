package com.petsaas.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petsaas.common.ApiResponse;
import com.petsaas.common.util.SecurityUtils;
import com.petsaas.core.entity.Product;
import com.petsaas.core.entity.ProductOrder;
import com.petsaas.core.service.ProductOrderService;
import com.petsaas.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/app/products")
@Tag(name = "App-商品商城")
public class AppProductController {

    @Autowired private ProductService productService;
    @Autowired private ProductOrderService orderService;

    @Operation(summary = "商品列表(支持搜索/分类)")
    @GetMapping("/list")
    public ApiResponse list(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size,
                            @RequestParam(required = false) String category,
                            @RequestParam(required = false) String keyword) {
        Page<Product> p = new Page<>(page, size);
        QueryWrapper<Product> query = new QueryWrapper<>();
        query.eq("status", 1); // 仅显示上架
        if (category != null && !category.isEmpty()) query.eq("category", category);
        if (keyword != null && !keyword.isEmpty()) query.like("name", keyword);
        query.orderByDesc("id"); // 新品在前
        return ApiResponse.success(productService.page(p, query));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public ApiResponse detail(@PathVariable Long id) {
        return ApiResponse.success(productService.getById(id));
    }

    @Operation(summary = "购买/创建订单")
    @PostMapping("/buy")
    public ApiResponse buy(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Long productId = Long.valueOf(payload.get("productId").toString());
            Integer quantity = (Integer) payload.getOrDefault("quantity", 1);

            ProductOrder order = orderService.createOrder(userId, productId, quantity);
            return ApiResponse.success(order); // 返回订单信息，前端拿到 orderNo 后调用支付接口
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "支付订单")
    @PostMapping("/pay")
    public ApiResponse pay(@RequestBody Map<String, String> payload) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            String orderNo = payload.get("orderNo");
            orderService.payOrder(userId, orderNo);
            return ApiResponse.success("支付成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}