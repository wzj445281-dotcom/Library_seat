package com.example.zhizuo.core.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AiRiskService {

    private static final String AI_RISK_URL = "http://localhost:5000/predict/risk";

    /**
     * 评估用户风险
     * @param creditScore 信用分
     * @return true=高风险(建议拦截/警告), false=低风险
     */
    public boolean isHighRiskUser(Integer creditScore) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("creditScore", creditScore);
            // 还可以传 param.put("historyViolations", user.getViolationCount());

            String resultJson = HttpUtil.post(AI_RISK_URL, JSONUtil.toJsonStr(param));
            JSONObject result = JSONUtil.parseObj(resultJson);

            if (result.getInt("code") == 200) {
                JSONObject data = result.getJSONObject("data");
                double prob = data.getDouble("riskProbability");
                String action = data.getStr("action");

                log.info("AI 风控评估: 信用分{}, 风险概率{}, 建议动作{}", creditScore, prob, action);
                return "WARN".equals(action) || "BLOCK".equals(action);
            }
        } catch (Exception e) {
            log.error("AI 风控服务调用失败，默认放行", e);
        }
        return false;
    }
}