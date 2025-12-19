package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.ResourceOrder;
import com.example.zhizuo.core.entity.Resources;
import com.example.zhizuo.core.mapper.ResourceOrderMapper;
import com.example.zhizuo.core.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app/resources")
@Tag(name = "Client-资源借阅服务")
public class AppResourceController {

    @Resource
    private ResourceService resourceService;

    @Resource
    private ResourceOrderMapper resourceOrderMapper;

    @Operation(summary = "查询资源列表(支持搜索/分类)")
    @GetMapping("/list")
    public ApiResponse<List<Resources>> list(@RequestParam(required = false) Integer type,
                                             @RequestParam(required = false) String keyword) {
        QueryWrapper<Resources> query = new QueryWrapper<>();
        query.eq("status", 1); // 仅查询已上架

        // 动态条件
        if (type != null) {
            query.eq("type", type);
        }
        if (keyword != null && !keyword.isEmpty()) {
            query.and(w -> w.like("name", keyword)
                    .or().like("author", keyword)
                    .or().like("isbn", keyword)
                    .or().like("location_code", keyword));
        }

        // 排序：有库存的优先，其次按更新时间
        query.orderByDesc("stock").orderByDesc("update_time");

        return ApiResponse.success(resourceService.list(query));
    }

    // ================== 借阅车 (Cart) 功能完善 ==================

    @Operation(summary = "加入借阅车")
    @PostMapping("/cart/add")
    public ApiResponse<String> addToCart(@RequestBody CartRequest request) {
        Long userId = getUserId();
        resourceService.addToCart(userId, request.getResourceId());
        return ApiResponse.success("已加入借阅车");
    }

    @Operation(summary = "移出借阅车")
    @PostMapping("/cart/remove")
    public ApiResponse<String> removeFromCart(@RequestBody CartRequest request) {
        Long userId = getUserId();
        resourceService.removeFromCart(userId, request.getResourceId());
        return ApiResponse.success("已移除");
    }

    @Operation(summary = "查看借阅车列表")
    @GetMapping("/cart/list")
    public ApiResponse<List<Resources>> getCartList() {
        Long userId = getUserId();
        return ApiResponse.success(resourceService.getCartList(userId));
    }

    @Operation(summary = "提交借阅车(批量借阅)")
    @PostMapping("/cart/submit")
    public ApiResponse<List<String>> submitCart(@RequestBody SubmitCartRequest request) {
        Long userId = getUserId();
        // 返回生成的订单号列表
        List<String> orderNos = resourceService.submitCart(userId, request.getDeliveryType());
        return ApiResponse.success(orderNos);
    }

    // ================== 原有接口保持兼容 ==================

    @Operation(summary = "直接借阅(单本)")
    @PostMapping("/borrow")
    public ApiResponse<String> borrow(@RequestBody BorrowRequest request) {
        Long userId = getUserId();
        String orderNo = resourceService.borrow(userId, request.getResourceId(), request.getDeliveryType());
        return ApiResponse.success(orderNo);
    }

    @Operation(summary = "我的借阅记录")
    @GetMapping("/my-orders")
    public ApiResponse<List<ResourceOrder>> myOrders() {
        Long userId = getUserId();
        QueryWrapper<ResourceOrder> query = new QueryWrapper<>();
        query.eq("user_id", userId).orderByDesc("create_time");
        return ApiResponse.success(resourceOrderMapper.selectList(query));
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getUserId() {
        try {
            String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            throw new RuntimeException("用户未登录或Token无效");
        }
    }

    @Data
    public static class BorrowRequest {
        private Long resourceId;
        private Integer deliveryType; // 0=自取, 1=配送
    }

    @Data
    public static class CartRequest {
        private Long resourceId;
    }

    @Data
    public static class SubmitCartRequest {
        private Integer deliveryType; // 0=自取, 1=配送
    }
}