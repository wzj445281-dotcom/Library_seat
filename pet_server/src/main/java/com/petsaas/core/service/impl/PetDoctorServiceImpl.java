package com.petsaas.core.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.PetDoctor;
import com.petsaas.core.mapper.PetDoctorMapper;
import com.petsaas.core.service.PetDoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 修复版 AI 医生问诊服务
 * 1. 修正包路径
 * 2. 继承 ServiceImpl 以支持 MyBatis-Plus 基础 CRUD
 * 3. 修复与 Python 通信的字段匹配问题
 */
@Slf4j
@Service
public class PetDoctorServiceImpl extends ServiceImpl<PetDoctorMapper, PetDoctor> implements PetDoctorService {

    @Override
    public List<PetDoctor> getActiveDoctors() {
        // 查询状态为 1 (在职/可预约) 的医生
        return this.list(new QueryWrapper<PetDoctor>().eq("status", 1));
    }

    @Override
    public String consult(String question) {
        String jsonResult = callAiService(question);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(jsonResult);
            // 兼容多种返回格式：优先取 answer (Python RAG 模块返回字段)，其次 response
            if (jsonObject.containsKey("answer")) {
                return jsonObject.getStr("answer");
            } else if (jsonObject.containsKey("response")) {
                return jsonObject.getStr("response");
            } else {
                return jsonResult;
            }
        } catch (Exception e) {
            log.warn("解析AI响应JSON失败，直接返回原文");
            return jsonResult;
        }
    }

    private String callAiService(String userMessage) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://ai-service:5000/chat");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(15000);

            // 【关键修复】双字段发送，同时满足 Python 端对 'question' 的需求和未来可能的 'message' 需求
            JSONObject param = new JSONObject();
            param.set("question", userMessage); // 适配 app.py
            param.set("message", userMessage);  // 冗余备份
            String jsonInputString = param.toString();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return response.toString();

        } catch (java.net.SocketTimeoutException e) {
            log.error("AI 服务响应超时");
            return "{\"answer\": \"医生正在紧急手术中，请稍后再试（连接超时）\"}";
        } catch (Exception e) {
            log.error("AI 服务调用异常", e);
            return "{\"answer\": \"系统正在维护中，请稍后重试。\"}";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}