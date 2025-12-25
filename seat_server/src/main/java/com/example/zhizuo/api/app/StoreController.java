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

    @Operation(summary = "获取菜单(商品列表)")
    @GetMapping("/menu")
    public ApiResponse<List<Product>> getMenu(@RequestParam(required = false) Long categoryId) {
        // 获取商品列表，取前100条作为菜单展示
        Page<Product> page = productService.getProductList(1, 100, null, categoryId);

        // 只返回上架状态(status=1)的商品
        List<Product> activeProducts = page.getRecords().stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .collect(Collectors.toList());

        return ApiResponse.success(activeProducts);
    }

    @Operation(summary = "创建订单")
    @PostMapping("/order/create")
    public ApiResponse<String> createOrder(@RequestBody Map<String, Object> params) {
        // 参数校验
        if (!params.containsKey("items") || !params.containsKey("deliveryType")) {
            return ApiResponse.error(400, "参数不完整");
        }

        try {
            Long userId = SecurityUtils.getUserId();
            List<Map<String, Object>> items = (List<Map<String, Object>>) params.get("items");
            Integer deliveryType = Integer.valueOf(params.get("deliveryType").toString());

            // 1. 调用核心下单逻辑 (扣库存 -> 生成订单)
            String orderNo = orderService.createOrder(userId, items, deliveryType);

            // 2. 发送订单超时延时消息 (RabbitMQ)
            // 消息发出后进入 TTL 队列，15分钟后若未被支付，将被 Consumer 处理
            try {
                orderMqProducer.sendOrderTimeoutMsg(orderNo);
            } catch (Exception e) {
                log.error("发送延时消息失败, 订单号: {}", orderNo, e);
                // 仅记录日志，不影响主流程返回
            }

            return ApiResponse.success(orderNo);
        } catch (Exception e) {
            log.error("下单异常", e);
            return ApiResponse.error(400, "下单失败: " + e.getMessage());
        }
    }

    @Operation(summary = "模拟支付")
    @PostMapping("/order/pay")
    public ApiResponse<String> payOrder(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");

        // 查询订单
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            return ApiResponse.error(404, "订单不存在");
        }

        // 幂等性检查
        if (order.getPayStatus() == 1) {
            return ApiResponse.success("订单已支付");
        }

        // 更新状态：直接流转为 PAID (制作中/配货中)
        // 在实际业务中，这里应该由微信支付的回调接口触发
        orderService.updateOrderStatus(order.getId(), "PAID");

        return ApiResponse.success("支付成功");
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/order/detail")
    public ApiResponse<Order> getOrderDetail(@RequestParam String orderNo) {
        Long userId = SecurityUtils.getUserId();
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)); // 安全检查：只能查自己的单

        if (order != null) {
            // 填充商品明细，方便前端展示
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            order.setProducts(items);
        }

        return ApiResponse.success(order);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/order/cancel")
    public ApiResponse<String> cancelOrder(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        Long userId = SecurityUtils.getUserId();

        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));

        if (order == null) {
            return ApiResponse.error(404, "订单不存在");
        }

        // 只有未支付或刚支付未制作的订单可以取消 (根据业务规则调整)
        if ("COMPLETED".equals(order.getStatus()) || "READY".equals(order.getStatus())) {
            return ApiResponse.error(400, "当前状态不可取消");
        }

        orderService.updateOrderStatus(order.getId(), "CANCELLED");
        // TODO: 这里应该补充 恢复库存 的逻辑

        return ApiResponse.success("订单已取消");
    }
}