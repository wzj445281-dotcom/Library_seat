package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.SecurityUtils;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端订单接口
 */
@RestController
@RequestMapping("/app/orders")
@Api(tags = "C端-订单管理")
public class AppOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    @ApiOperation("获取我的订单列表")
    public ApiResponse<IPage<Order>> getMyOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        Long userId = SecurityUtils.getUserId();

        Page<Order> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        // 使用 Service 封装好的方法
        IPage<Order> result = orderService.pageWithItems(pageParam, wrapper);

        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取订单详情")
    public ApiResponse<Order> getOrderDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();

        // 使用 Service 封装好的方法
        Order order = orderService.getDetailWithItems(id);

        if (order == null) {
            return ApiResponse.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return ApiResponse.error("无权访问该订单");
        }

        return ApiResponse.success(order);
    }

    @PostMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public ApiResponse<Boolean> cancelOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        Order order = orderService.getById(id);

        if (order == null || !order.getUserId().equals(userId)) {
            return ApiResponse.error("订单不存在或无权操作");
        }
        if (order.getStatus() != 0) {
            return ApiResponse.error("当前状态无法取消");
        }

        order.setStatus(4);
        boolean success = orderService.updateById(order);
        return ApiResponse.success(success);
    }

    @PostMapping
    @ApiOperation("创建订单")
    public ApiResponse<Order> createOrder(@RequestBody Order order) {
        Long userId = SecurityUtils.getUserId();
        order.setUserId(userId);
        // 实际开发中建议将创建逻辑也封装到 Service 的 createOrder 方法中
        orderService.save(order);
        return ApiResponse.success(order);
    }
}