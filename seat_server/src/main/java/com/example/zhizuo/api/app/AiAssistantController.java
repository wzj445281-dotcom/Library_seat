package com.example.zhizuo.api.app;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.service.OrderService;
import com.example.zhizuo.core.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 助手控制器 (增强版)
 * 集成商品库、用户收藏、历史订单数据，打造个性化导购
 */
@RestController
@RequestMapping("/app/ai")
public class AiAssistantController {

    @Autowired
    private ProductService productService;

    // 从 application.yml 读取配置，避免硬编码
    @Value("${ai.deepseek.key:}")
    private String deepSeekApiKey;

    @Value("${ai.deepseek.url:https://api.deepseek.com/chat/completions}")
    private String deepSeekApiUrl;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String deepSeekModel;

    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, String> body, @RequestAttribute(required = false) Long userId) {
        String userMessage = body.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ApiResponse.error("输入不能为空");
        }

        try {
            // 1. 【构建知识库 (Context)】

            // A. 商品列表 (模拟全量，实际应用可结合 Vector DB)
            List<Product> products = productService.list();
            String productContext = products.stream()
                    .map(p -> String.format("{id:%d, name:'%s', price:%.2f, tags:'%s'}",
                            p.getId(), p.getName(), p.getPrice(), "新品,热销")) // 模拟 Tags
                    .collect(Collectors.joining(", "));

            // B. 用户画像 (收藏 + 历史订单)
            String userProfile = buildUserProfile(userId);

            // 2. 【Prompt 工程】
            // 核心：明确角色、输入数据、输出格式
            String systemPrompt = String.format(
                    "你是一个瑞幸咖啡的资深AI导购助手。请根据用户的【历史偏好】和【当前问题】，从【商品库】中推荐最合适的饮品。\n\n" +
                            "=== 数据输入 ===\n" +
                            "【商品库】：[%s]\n" +
                            "【用户画像】：%s\n\n" +
                            "=== 输出规则 ===\n" +
                            "1. 语气亲切、活泼，像朋友一样交流。\n" +
                            "2. 如果用户有收藏或常喝的口味，优先推荐相关商品。\n" +
                            "3. IMPORTANT: 必须返回严格的 JSON 格式，不要包含 Markdown 标记。\n" +
                            "4. JSON 结构：\n" +
                            "{\n" +
                            "  \"reply\": \"回复文本，结合用户偏好进行个性化推荐\",\n" +
                            "  \"recommendations\": [\n" +
                            "    {\"id\": 商品ID, \"name\": \"商品名\", \"price\": 价格, \"image\": \"商品图URL(可留空)\", \"reason\": \"推荐理由(如: 您最爱的生椰口味)\"}\n" +
                            "  ]\n" +
                            "}",
                    productContext, userProfile
            );

            // 3. 调用 DeepSeek API
            // 检查 API Key
            if (deepSeekApiKey == null || deepSeekApiKey.isEmpty() || deepSeekApiKey.startsWith("sk-your")) {
                // 演示模式：如果没配置 Key，返回 Mock 数据
                return ApiResponse.success(mockAiResponse(userMessage));
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepSeekApiKey);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", deepSeekModel);
            requestBody.put("temperature", 0.7);

            ArrayNode messages = requestBody.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt);
            messages.addObject().put("role", "user").put("content", userMessage);

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(deepSeekApiUrl, entity, 
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

            // 4. 解析结果
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        String content = (String) message.get("content");
                        return ApiResponse.success(parseAiJson(content));
                    }
                }
            }

            return ApiResponse.error("AI 响应异常");

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.success(mockAiResponse(userMessage)); // 降级处理
        }
    }

    /**
     * 构建用户画像 (模拟数据，实际应查数据库)
     */
    private String buildUserProfile(Long userId) {
        if (userId == null) return "新用户，无历史数据";

        // 模拟查库逻辑
        // List<String> favorites = favoriteService.getUserFavorites(userId);
        // List<Order> orders = orderService.getRecentOrders(userId);

        return "老用户，ID:" + userId +
                "，【收藏列表】：['生椰拿铁', '丝绒拿铁']" +
                "，【历史偏好】：喜欢‘少糖’、‘去冰’，经常在下午 2 点下单。";
    }

    /**
     * 解析 AI 返回的 JSON 字符串 (包含容错处理)
     */
    private Map<String, Object> parseAiJson(String jsonContent) {
        try {
            // 清理 Markdown 标记
            String cleanJson = jsonContent.trim();
            if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
            if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
            if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = new ObjectMapper().readValue(cleanJson, Map.class);
            return result;
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("reply", jsonContent); // 解析失败则直接把文本作为回复
            fallback.put("recommendations", new ArrayList<>());
            return fallback;
        }
    }

    /**
     * Mock 数据 (用于演示或 API Key 无效时)
     */
    private Map<String, Object> mockAiResponse(String userMsg) {
        Map<String, Object> res = new HashMap<>();
        if (userMsg.contains("推荐")) {
            res.put("reply", "根据您的口味（喜欢生椰），我强烈推荐您试试我们的【生椰拿铁】！新品【冰吸生椰】也很不错哦~ 🥥");
            List<Map<String, Object>> recs = new ArrayList<>();
            Map<String, Object> item1 = new HashMap<>();
            item1.put("id", 1);
            item1.put("name", "生椰拿铁");
            item1.put("price", 18.0);
            item1.put("reason", "您的收藏首选");
            item1.put("image", "[https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200](https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200)");
            recs.add(item1);
            res.put("recommendations", recs);
        } else {
            res.put("reply", "我是瑞幸 AI 助手，请问有什么可以帮您？输入“推荐”试试看！(API Key 未配置，正在运行 Mock 模式)");
            res.put("recommendations", new ArrayList<>());
        }
        return res;
    }
}