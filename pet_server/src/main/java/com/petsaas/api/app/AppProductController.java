package com.petsaas.api.app; 

import com.petsaas.common.ApiResponse; 
import com.petsaas.common.util.SecurityUtils; 
import com.petsaas.core.service.ProductService; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*; 

import java.util.Map; 

@RestController 
@RequestMapping("/api/app/products") 
public class AppProductController { 

    @Autowired 
    private ProductService productService; 

    // 获取商品列表 
    @GetMapping("/list") 
    public ApiResponse list(@RequestParam(required = false) String category) { 
        return ApiResponse.success(productService.getProductList(category)); 
    } 

    // 购买商品 
    @PostMapping("/buy") 
    public ApiResponse buy(@RequestBody Map<String, Object> payload) { 
        try { 
            Long userId = SecurityUtils.getCurrentUserId(); // 假设有此工具类获取当前登录用户ID 
            Long productId = Long.valueOf(payload.get("productId").toString()); 
            Integer quantity = Integer.valueOf(payload.get("quantity").toString()); 
             
            productService.buyProduct(userId, productId, quantity); 
            return ApiResponse.success("购买成功"); 
        } catch (Exception e) { 
            return ApiResponse.error(e.getMessage()); 
        } 
    } 
}