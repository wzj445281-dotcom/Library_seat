package com.example.zhizuo.api.websocket;

import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 控制器
 * 用于订单状态实时推送
 */
@Slf4j
@RestController
@RequestMapping("/api/websocket")
public class OrderWebSocketController {

    @Resource
    private OrderService orderService;

    /**
     * 推送订单状态更新
     * 当订单状态改变时调用此方法
     */
    public void pushOrderStatus(String orderNo, String status) {
        OrderWebSocketEndpoint.pushOrderStatus(orderNo, status);
    }

    /**
     * 测试接口：手动触发订单状态推送
     */
    @RequestMapping("/test/{orderNo}")
    public Map<String, Object> testPush(@PathVariable String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order != null) {
            pushOrderStatus(orderNo, order.getStatus());
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "推送成功");
            return result;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 404);
            result.put("message", "订单不存在");
            return result;
        }
    }
}

