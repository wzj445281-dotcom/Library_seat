package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.entity.OrderItem;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.entity.UserFavorite;
import com.example.zhizuo.core.mapper.OrderItemMapper;
import com.example.zhizuo.core.mapper.ProductMapper;
import com.example.zhizuo.core.service.AiPredictionService;
import com.example.zhizuo.core.service.OrderService;
import com.example.zhizuo.core.service.ProductService;
import com.example.zhizuo.mapper.UserFavoriteMapper;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 助手控制器 (增强版)
 * 集成商品库、用户收藏、历史订单数据，打造个性化导购
 * 同时提供客流拥挤度预测接口
 */
@RestController
@RequestMapping("/api/app/ai")
public class AiAssistantController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    // ✅ 新增：注入 AI 预测服务
    @Autowired
    private AiPredictionService aiPredictionService;

    // 从 application.yml 读取配置
    @Value("${ai.deepseek.key:}")
    private String deepSeekApiKey;

    @Value("${ai.deepseek.url:https://api.deepseek.com/chat/completions}")
    private String deepSeekApiUrl;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String deepSeekModel;

    /**
     * AI 智能导购对话接口
     */
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, String> body, @RequestAttribute(required = false) Long userId) {
        String userMessage = body.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ApiResponse.error("输入不能为空");
        }

        try {
            // 1. 【构建知识库 (Context) - 从数据库获取真实数据】

            // A. 商品列表 - 从数据库获取所有上架商品
            List<Product> products = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getStatus, 1) // 只获取上架商品
                            .orderByDesc(Product::getSales) // 按销量排序
            );

            // 构建商品知识库 JSON 格式
            String productContext = products.stream()
                    .map(p -> String.format(
                            "{\"pid\":%d, \"name\":\"%s\", \"price\":%.2f, \"image\":\"%s\", \"description\":\"%s\", \"sales\":%d}",
                            p.getId(),
                            p.getName() != null ? p.getName().replace("\"", "'") : "",
                            p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO,
                            p.getImgUrl() != null ? p.getImgUrl() : "",
                            p.getDescription() != null ? p.getDescription().replace("\"", "'").replace("\n", " ") : "",
                            p.getSales() != null ? p.getSales() : 0
                    ))
                    .collect(Collectors.joining(", "));

            // B. 用户画像 - 从数据库获取收藏和历史订单
            String userProfile = buildUserProfile(userId);

            // 2. 【Prompt 工程 + 结构化输出】
            String systemPrompt = String.format(
                    "你是一个瑞幸咖啡的资深AI导购助手。请根据用户的【历史偏好】和【当前问题】，从【商品库】中推荐最合适的饮品。\n\n" +
                            "=== 数据输入 ===\n" +
                            "【商品库】：[%s]\n" +
                            "【用户画像】：%s\n\n" +
                            "=== 输出规则 ===\n" +
                            "1. 语气亲切、活泼，像朋友一样交流。\n" +
                            "2. 如果用户有收藏或常喝的口味，优先推荐相关商品。\n" +
                            "3. 推荐商品时，必须从商品库中选择，使用准确的 pid、name、price、image。\n" +
                            "4. 回复要自然、有温度，不要生硬地列举商品。",
                    productContext, userProfile
            );

            // 3. 调用 DeepSeek API
            // 检查 API Key - 如果未配置，提示用户配置
            if (deepSeekApiKey == null || deepSeekApiKey.isEmpty() || deepSeekApiKey.startsWith("sk-your") || deepSeekApiKey.equals("${AI_DEEPSEEK_KEY}")) {
                // 如果没配置 Key，返回提示信息，同时返回 Mock 数据作为降级方案
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

            // 使用结构化输出（JSON Schema）
            ObjectNode responseFormat = mapper.createObjectNode();
            responseFormat.put("type", "json_object");
            requestBody.set("response_format", responseFormat);

            ArrayNode messages = requestBody.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt +
                    "\n\n请严格按照以下 JSON 格式返回，不要包含任何 Markdown 标记或额外文本：\n" +
                    "{\n" +
                    "  \"reply\": \"回复文本，结合用户偏好进行个性化推荐\",\n" +
                    "  \"recommendations\": [\n" +
                    "    {\"pid\": 商品ID(数字), \"name\": \"商品名称\", \"price\": 价格(数字), \"image\": \"图片URL\", \"reason\": \"推荐理由\"}\n" +
                    "  ]\n" +
                    "}");
            messages.addObject().put("role", "user").put("content", userMessage);

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(deepSeekApiUrl, entity, Map.class);

            // 4. 解析结果
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
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
     * ✅ 新增接口：获取人流量/拥挤度预测
     * 供 monitor.html 大屏使用
     * @return 预测的拥挤度系数 (0.0 - 1.0)
     */
    @GetMapping("/prediction/crowding")
    public ApiResponse<Double> getCrowdingPrediction() {
        // 调用 Python 预测服务 (通过 AiPredictionService)
        Double prediction = aiPredictionService.predictCrowding();
        return ApiResponse.success(prediction);
    }

    /**
     * 构建用户画像 - 从数据库获取真实数据
     */
    private String buildUserProfile(Long userId) {
        if (userId == null) {
            return "新用户，无历史数据";
        }

        StringBuilder profile = new StringBuilder();
        profile.append("用户ID: ").append(userId);

        // 1. 获取收藏列表
        List<UserFavorite> favorites = userFavoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId.toString())
                        .orderByDesc(UserFavorite::getCreateTime)
                        .last("LIMIT 10")
        );

        if (!favorites.isEmpty()) {
            List<Long> favoriteProductIds = favorites.stream()
                    .map(UserFavorite::getProductId)
                    .collect(Collectors.toList());

            List<Product> favoriteProducts = productMapper.selectBatchIds(favoriteProductIds);
            String favoriteNames = favoriteProducts.stream()
                    .map(Product::getName)
                    .collect(Collectors.joining("、"));

            profile.append("，【收藏商品】：[").append(favoriteNames).append("]");
        }

        // 2. 获取历史订单（最近10单）
        List<Order> recentOrders = orderService.getUserOrderList(userId, null);
        if (recentOrders.size() > 10) {
            recentOrders = recentOrders.subList(0, 10);
        }

        if (!recentOrders.isEmpty()) {
            // 统计订单中最常购买的商品
            Map<Long, Integer> productCountMap = new HashMap<>();
            for (Order order : recentOrders) {
                List<OrderItem> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>()
                                .eq(OrderItem::getOrderId, order.getId())
                );
                for (OrderItem item : items) {
                    productCountMap.put(item.getProductId(),
                            productCountMap.getOrDefault(item.getProductId(), 0) + item.getQuantity());
                }
            }

            // 获取最常购买的商品名称
            if (!productCountMap.isEmpty()) {
                Long topProductId = productCountMap.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);

                if (topProductId != null) {
                    Product topProduct = productMapper.selectById(topProductId);
                    if (topProduct != null) {
                        profile.append("，【历史订单】：最近购买了").append(recentOrders.size()).append("单");
                        profile.append("，最常购买：").append(topProduct.getName());
                    }
                }
            }
        } else {
            profile.append("，【历史订单】：暂无订单记录");
        }

        return profile.toString();
    }

    /**
     * 解析 AI 返回的 JSON 字符串 (包含容错处理)
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAiJson(String jsonContent) {
        try {
            // 清理 Markdown 标记
            String cleanJson = jsonContent.trim();
            if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
            if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
            if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            cleanJson = cleanJson.trim();

            Map<String, Object> result = new ObjectMapper().readValue(cleanJson, Map.class);

            // 验证并补充推荐商品信息（从数据库获取完整信息）
            if (result.containsKey("recommendations")) {
                List<Map<String, Object>> recommendations = (List<Map<String, Object>>) result.get("recommendations");
                List<Map<String, Object>> enrichedRecommendations = new ArrayList<>();

                for (Map<String, Object> rec : recommendations) {
                    Object pidObj = rec.get("pid");
                    if (pidObj != null) {
                        Long pid = null;
                        if (pidObj instanceof Number) {
                            pid = ((Number) pidObj).longValue();
                        } else if (pidObj instanceof String) {
                            try {
                                pid = Long.parseLong((String) pidObj);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                        }

                        if (pid != null) {
                            Product product = productMapper.selectById(pid);
                            if (product != null) {
                                Map<String, Object> enrichedRec = new HashMap<>();
                                enrichedRec.put("pid", product.getId());
                                enrichedRec.put("name", product.getName());
                                enrichedRec.put("price", product.getPrice());
                                enrichedRec.put("image", product.getImgUrl() != null ? product.getImgUrl() : "");
                                enrichedRec.put("reason", rec.getOrDefault("reason", "为您推荐"));
                                enrichedRecommendations.add(enrichedRec);
                            }
                        }
                    }
                }

                result.put("recommendations", enrichedRecommendations);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
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

        // 从数据库获取热门商品作为 Mock 推荐
        List<Product> hotProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getSales)
                        .last("LIMIT 3")
        );

        if (userMsg.contains("推荐") || userMsg.contains("好喝") || userMsg.contains("菜单")) {
            res.put("reply", "根据您的需求，我为您推荐以下热门商品！这些都是我们店里的爆款哦~ ☕️");
            List<Map<String, Object>> recs = new ArrayList<>();
            for (Product product : hotProducts) {
                Map<String, Object> item = new HashMap<>();
                item.put("pid", product.getId());
                item.put("name", product.getName());
                item.put("price", product.getPrice());
                item.put("image", product.getImgUrl() != null ? product.getImgUrl() : "");
                item.put("reason", "热门推荐");
                recs.add(item);
            }
            res.put("recommendations", recs);
        } else {
            res.put("reply", "您好！我是瑞幸 AI 助手，可以帮您推荐商品、解答问题。试试问我\"推荐\"或\"什么好喝\"吧！");
            res.put("recommendations", new ArrayList<>());
        }
        return res;
    }
}