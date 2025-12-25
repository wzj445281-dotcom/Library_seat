package com.petsaas.api.app;

import com.petsaas.common.ApiResponse;
import com.petsaas.common.util.SecurityUtils;
import com.petsaas.core.service.ProductOrderService;
import com.petsaas.core.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/app/products")
public class AppProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductOrderService productOrderService;

    // 获取商品列表
    @GetMapping("/list")
    public ApiResponse list(@RequestParam(required = false) String category) {
        return ApiResponse.success(productService.getProductList(category));
    }

    // 购买商品 (模拟支付闭环)
    @PostMapping("/buy")
    public ApiResponse buy(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            // 如果 SecurityUtils 还没准备好，这里先 hardcode 一个 ID 方便测试
            if (userId == null) userId = 1L;

            Long productId = Long.valueOf(payload.get("productId").toString());
            Integer quantity = Integer.valueOf(payload.get("quantity").toString());

            // 调用带分布式锁和服务流水的创建订单逻辑
            String orderNo = productOrderService.createOrder(userId, productId, quantity);

            return ApiResponse.success(Map.of("message", "支付成功", "orderNo", orderNo));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
}