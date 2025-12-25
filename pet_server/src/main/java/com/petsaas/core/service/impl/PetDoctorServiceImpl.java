package com.petsaas.core.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.petsaas.core.service.PetDoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PetDoctorServiceImpl implements PetDoctorService {

    // docker-compose 中 Python 服务的服务名是 ai-service，端口 5000
    private static final String AI_URL = "http://ai-service:5000/chat";

    @Override
    public String consult(String question) {
        Map<String, Object> param = new HashMap<>();
        param.put("question", question);

        try {
            // 使用 Hutool 发送 POST 请求
            String resultJson = HttpUtil.post(AI_URL, JSONUtil.toJsonStr(param), 3000); // 3秒超时

            // 解析返回: {"code": 200, "data": {"answer": "..."}}
            JSONObject json = JSONUtil.parseObj(resultJson);
            if (json.getInt("code") == 200) {
                return json.getJSONObject("data").getStr("answer");
            }
        } catch (Exception e) {
            log.error("调用AI服务失败: {}", e.getMessage());
        }
        return "AI 医生暂时掉线了，请稍后再试。";
    }
}