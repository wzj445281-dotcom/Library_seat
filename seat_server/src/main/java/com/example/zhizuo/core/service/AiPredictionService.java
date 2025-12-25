package com.example.zhizuo.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 预测与对话服务
 * 负责与 Python AI 服务进行通信
 */
@Service
@Slf4j
public class AiPredictionService {

    // Python 服务的地址，默认指向本地或Docker服务名
    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用 AI 服务进行对话
     */
    public String chatWithAi(String userMessage) {
        String url = aiServiceUrl + "/chat";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("message", userMessage);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to AI Service: {}", url);
            // 发送 POST 请求
            String response = restTemplate.postForObject(url, request, String.class);

            // 解析返回的 JSON
            JsonNode rootNode = objectMapper.readTree(response);
            if (rootNode.has("reply")) {
                return rootNode.get("reply").asText();
            } else {
                return "AI 服务返回数据格式异常";
            }

        } catch (Exception e) {
            log.error("Error communicating with AI service", e);
            return "抱歉，AI 助手暂时开小差了，请稍后再试。";
        }
    }

    /**
     * 预测座位拥挤度 (保留原有功能)
     */
    public Double predictCrowding() {
        String url = aiServiceUrl + "/predict";
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("timestamp", System.currentTimeMillis());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            if (response != null && response.containsKey("prediction")) {
                Object prediction = response.get("prediction");
                return Double.valueOf(prediction.toString());
            }
        } catch (Exception e) {
            log.error("Error calling predict service", e);
        }
        return 0.5; // 出错时返回默认拥挤度
    }

    /**
     * 同步热力图分数 (修复编译错误)
     * 供 DemoController 调用
     */
    public void syncHeatScores() {
        log.info("正在执行热力图数据同步 (Demo模式)...");
        Double score = predictCrowding();
        log.info("同步完成，当前区域热度: {}", score);
    }
}