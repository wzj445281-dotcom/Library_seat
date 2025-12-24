package com.example.zhizuo.api.app;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@Tag(name = "App-商品模块")
public class AppProductController {

    private final ProductService productService;

    public AppProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "获取所有商品")
    @GetMapping("/list")
    public ApiResponse<List<Product>> list() {
        return ApiResponse.success(productService.list());
    }

    @Operation(summary = "根据分类获取商品")
    @GetMapping("/category/{category}")
    public ApiResponse<List<Product>> getByCategory(@PathVariable String category) {
        // 这里可以根据category参数查询对应分类的商品
        // 由于Product实体中category是Integer类型，需要转换
        return ApiResponse.success(productService.list());
    }

    @Operation(summary = "获取热销商品")
    @GetMapping("/top-selling")
    public ApiResponse<List<Product>> getTopSelling(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.success(productService.getTopSellingProducts(limit));
    }

    @Operation(summary = "购买商品")
    @PostMapping("/purchase")
    public ApiResponse<String> purchase(@RequestParam Long userId, 
                                       @RequestParam Long productId, 
                                       @RequestParam Integer quantity) {
        try {
            String result = productService.purchase(userId, productId, quantity);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @Operation(summary = "添加到购物车")
    @PostMapping("/add-to-cart")
    public ApiResponse<String> addToCart(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            productService.addToCart(userId, productId);
            return ApiResponse.success("添加成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @Operation(summary = "从购物车移除")
    @PostMapping("/remove-from-cart")
    public ApiResponse<String> removeFromCart(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            productService.removeFromCart(userId, productId);
            return ApiResponse.success("移除成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }

    @Operation(summary = "获取购物车")
    @GetMapping("/cart/{userId}")
    public ApiResponse<List<Product>> getCart(@PathVariable Long userId) {
        return ApiResponse.success(productService.getCartList(userId));
    }

    @Operation(summary = "提交购物车")
    @PostMapping("/submit-cart/{userId}")
    public ApiResponse<List<String>> submitCart(@PathVariable Long userId) {
        try {
            return ApiResponse.success(productService.submitCart(userId));
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}