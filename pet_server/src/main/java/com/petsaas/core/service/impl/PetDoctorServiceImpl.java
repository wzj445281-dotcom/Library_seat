package com.petsaas.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.petsaas.service.PetDoctorService; // 请根据你的项目实际包名修改
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AI 医生问诊服务实现类
 * 包含完整的超时控制和 HTTP 调用逻辑
 */
@Slf4j
@Service
public class PetDoctorServiceImpl implements PetDoctorService {

    // 1. 公共接口实现：对外提供服务
    @Override
    public String consult(String question) {
        // 调用下方定义的私有核心方法，获取原始 JSON 字符串
        String jsonResult = callAiService(question);

        try {
            // 使用 Hutool 解析返回的 JSON 字符串
            // 预期格式: {"response": "猫咪需要多喝水...", "code": 200}
            JSONObject jsonObject = JSONUtil.parseObj(jsonResult);

            // 提取内容 (优先取 response 字段，兼容性处理)
            if (jsonObject.containsKey("response")) {
                return jsonObject.getStr("response");
            } else if (jsonObject.containsKey("answer")) {
                return jsonObject.getStr("answer");
            } else {
                // 如果没有标准字段，直接返回整段内容作为兜底
                return jsonResult;
            }
        } catch (Exception e) {
            log.warn("解析AI响应JSON失败，直接返回原文: {}", e.getMessage());
            // 如果解析失败（比如 Python 端报错返回了 HTML），为了不让前端崩，直接返回原文
            return jsonResult;
        }
    }

    /**
     * 2. 核心私有方法：负责与 Python 容器通信
     * 使用原生 JDK 实现，确保无依赖且超时可控
     */
    private String callAiService(String userMessage) {
        HttpURLConnection conn = null;
        try {
            // Docker 内部域名 ai-service，端口 5000 (请确保 docker-compose.yml 中 Python 服务名为 ai-service)
            URL url = new URL("http://ai-service:5000/chat");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // 【关键】设置超时，防止卡死
            conn.setConnectTimeout(3000); // 3秒连不上报错
            conn.setReadTimeout(15000);   // AI思考超过15秒算超时

            // 构建 JSON: {"message": "用户的问题"}
            // 简单转义双引号，防止 JSON 格式错误
            String safeMessage = userMessage.replace("\"", "\\\"");
            String jsonInputString = "{\"message\": \"" + safeMessage + "\"}";

            // 发送数据
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 接收响应
            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return response.toString();

        } catch (java.net.SocketTimeoutException e) {
            log.error("AI 服务响应超时");
            return "{\"response\": \"猫咪医生正在忙碌手术中（请求超时），请稍后再试喵~\"}";
        } catch (Exception e) {
            log.error("AI 服务调用异常", e);
            return "{\"response\": \"系统正在维护中，请稍后重试。\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}