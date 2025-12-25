package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.SecurityUtils;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.entity.OrderItem;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.mapper.OrderItemMapper;
import com.example.zhizuo.core.mq.OrderMqProducer;
import com.example.zhizuo.core.service.OrderService;
import com.example.zhizuo.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/app/store")
@Tag(name = "App-书局点单(O2O)")
public class StoreController {

    @Resource
    private ProductService productService;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private OrderMqProducer orderMqProducer;

    @Operation(summary = "获取菜单")
    @GetMapping("/menu")
    public ApiResponse<List<Product>> getMenu(@RequestParam(required = false) Long categoryId) {
        Page<Product> page = productService.getProductList(1, 100, null, categoryId);
        List<Product> activeProducts = page.getRecords().stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .collect(Collectors.toList());
        return ApiResponse.success(activeProducts);
    }

    @Operation(summary = "创建订单")
    @PostMapping("/order/create")
    public ApiResponse<String> createOrder(@RequestBody Map<String, Object> params) {
        if (!params.containsKey("items") || !params.containsKey("deliveryType")) {
            return ApiResponse.error(400, "参数不完整");
        }
        try {
            Long userId = SecurityUtils.getUserId();
            List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");
            Integer deliveryType = Integer.valueOf(params.get("deliveryType").toString());

            String orderNo = orderService.createOrder(userId, items, deliveryType);

            // 发送延时消息 (TTL 15分钟)
            try {
                orderMqProducer.sendOrderTimeoutMsg(orderNo);
            } catch (Exception e) {
                log.error("发送延时消息失败: {}", orderNo, e);
            }
            return ApiResponse.success(orderNo);
        } catch (Exception e) {
            log.error("下单异常", e);
            return ApiResponse.error(400, "下单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "取消订单")
    @PostMapping("/order/cancel")
    public ApiResponse<String> cancelOrder(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        Long userId = SecurityUtils.getUserId();

        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));

        if (order == null) return ApiResponse.error(404, "订单不存在");

        if ("COMPLETED".equals(order.getStatus())) {
            return ApiResponse.error(400, "订单已完成，无法取消");
        }

        // 调用新的回滚逻辑
        orderService.closeOrderAndRestoreStock(order.getId(), "用户主动取消");
        return ApiResponse.success("订单已取消");
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/order/list")
    public ApiResponse<List<Order>> getMyOrders(@RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getUserId();
        return ApiResponse.success(orderService.getUserOrderList(userId, status));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/order/detail")
    public ApiResponse<Order> getOrderDetail(@RequestParam String orderNo) {
        Long userId = SecurityUtils.getUserId();
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));

        if (order != null) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            order.setProducts(items);
        }
        return ApiResponse.success(order);
    }

    // 支付接口保持不变...
    @Operation(summary = "模拟支付")
    @PostMapping("/order/pay")
    public ApiResponse<String> payOrder(@RequestBody Map<String, Object> params) {
        // ... (保持原样)
        String orderNo = (String) params.get("orderNo");
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) return ApiResponse.error(404, "订单不存在");
        orderService.updateOrderStatus(order.getId(), "PAID");
        return ApiResponse.success("支付成功");
    }
}