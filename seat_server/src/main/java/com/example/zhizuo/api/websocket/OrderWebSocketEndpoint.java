package com.example.zhizuo.api.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 端点（原生 WebSocket，支持微信小程序）
 * 用于订单状态实时推送
 */
@Slf4j
@Component
@ServerEndpoint("/ws/order/{orderNo}")
public class OrderWebSocketEndpoint {

    // 存储每个订单号对应的 WebSocket 会话
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("orderNo") String orderNo) {
        log.info("WebSocket 连接建立: orderNo={}, sessionId={}", orderNo, session.getId());
        sessionMap.put(orderNo, session);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("orderNo") String orderNo) {
        log.info("WebSocket 连接关闭: orderNo={}, sessionId={}", orderNo, session.getId());
        sessionMap.remove(orderNo);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("orderNo") String orderNo) {
        log.info("收到 WebSocket 消息: orderNo={}, message={}", orderNo, message);
        // 可以处理客户端发送的消息，这里暂时不需要
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("orderNo") String orderNo) {
        log.error("WebSocket 错误: orderNo={}, error={}", orderNo, error.getMessage(), error);
    }

    /**
     * 推送订单状态更新
     * 静态方法，供外部调用
     */
    public static void pushOrderStatus(String orderNo, String status) {
        Session session = sessionMap.get(orderNo);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = new java.util.HashMap<>();
                message.put("type", "ORDER_STATUS_UPDATE");
                message.put("orderNo", orderNo);
                message.put("status", status);
                message.put("timestamp", System.currentTimeMillis());

                String jsonMessage = JSON.toJSONString(message);
                session.getBasicRemote().sendText(jsonMessage);
                log.info("推送订单状态更新成功: orderNo={}, status={}", orderNo, status);
            } catch (IOException e) {
                log.error("推送订单状态更新失败: orderNo={}", orderNo, e);
            }
        } else {
            log.warn("订单 {} 的 WebSocket 会话不存在或已关闭", orderNo);
        }
    }
}

