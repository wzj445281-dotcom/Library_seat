package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.mapper.ProductMapper;
import com.example.zhizuo.core.service.AiPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手控制器 (本地离线版)
 * 逻辑简化说明：
 * 1. 不再连接 DeepSeek，也不需要构建复杂的用户画像 Prompt。
 * 2. 直接将用户消息转发给本地 Python 服务 (/chat 接口)。
 * 3. 如果 Python 服务未启动，使用 Java 内部的 Mock 数据兜底。
 */
@RestController
@RequestMapping("/api/app/ai")
public class AiAssistantController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AiPredictionService aiPredictionService;

    // 读取配置文件中的 AI 服务地址，默认指向本地 Python 服务
    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    /**
     * AI 智能导购对话接口
     * 逻辑：Java 接收前端请求 -> 转发给 Python -> 返回结果
     */
    @PostMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ApiResponse.error("输入不能为空");
        }

        try {
            // 1. 准备 Python 服务的 URL (拼接 /chat)
            String pythonChatUrl = aiServiceUrl + "/chat";
            System.out.println("调用Python服务URL: " + pythonChatUrl);

            // 2. 构造发送给 Python 的请求体
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("message", userMessage);
            System.out.println("发送给Python的消息: " + userMessage);

            // 3. 设置请求头，确保UTF-8编码
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 4. 发送 HTTP POST 请求 (Java -> Python)
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);
            System.out.println("发送请求到Python服务...");
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonChatUrl, request, Map.class);
            System.out.println("收到Python服务响应: " + response.getStatusCode());

            // 5. 处理响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Python 已经返回了符合前端要求的 JSON 格式（包含 reply 和 recommendations）
                // 我们直接原样返回给前端即可
                return ApiResponse.success(response.getBody());
            } else {
                return ApiResponse.error("AI 服务响应异常");
            }

        } catch (Exception e) {
            // 打印错误日志，方便调试（比如 Connection refused 说明 Python 没启动）
            System.err.println("调用本地 Python AI 服务失败: " + e.getMessage());

            // 5. 降级处理 (关键！)
            // 如果 Python 服务挂了或没启动，不要报错，而是返回 Java 写死的 Mock 数据
            // 这样能保证前端演示时不会出现“系统错误”
            return ApiResponse.success(mockAiResponse(userMessage));
        }
    }

    /**
     * 获取人流量/拥挤度预测
     * 供 monitor.html 大屏使用
     */
    @GetMapping("/prediction/crowding")
    public ApiResponse<Double> getCrowdingPrediction() {
        // 这个接口依然保留，调用 Python 的预测功能
        Double prediction = aiPredictionService.predictCrowding();
        return ApiResponse.success(prediction);
    }

    /**
     * Mock 数据 (兜底方案)
     * 当 Python 服务不可用时，使用此简单的规则回复，保证演示不翻车
     */
    private Map<String, Object> mockAiResponse(String userMsg) {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> recs = new ArrayList<>();

        // 简单的关键词匹配
        if (userMsg.contains("推荐") || userMsg.contains("好喝") || userMsg.contains("菜单")) {
            res.put("reply", "（网络开小差了，这是备用回复）给您推荐我们的招牌生椰拿铁！🥥");

            // 从数据库里随便查 2 个销量高的商品作为推荐
            List<Product> hotProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getStatus, 1) // 上架状态
                            .orderByDesc(Product::getSales) // 按销量
                            .last("LIMIT 2")
            );

            for (Product p : hotProducts) {
                Map<String, Object> item = new HashMap<>();
                item.put("pid", p.getId());
                item.put("name", p.getName());
                item.put("price", p.getPrice());
                // 确保图片路径不为空
                item.put("image", p.getImgUrl() != null ? p.getImgUrl() : "");
                item.put("reason", "热门榜单");
                recs.add(item);
            }
        } else {
            res.put("reply", "（网络开小差了）您好，我是瑞幸小助手。由于连接不到 AI 大脑，我现在只能回答简单问题。");
        }

        res.put("recommendations", recs);
        return res;
    }
}