package com.petsaas.core.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.core.entity.CreditLog;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.CreditLogMapper;
import com.petsaas.core.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 简易 AI 风控服务
 * 实际项目中可接入 Python 异常检测模型
 */
@Service
public class AiRiskService {

    @Autowired private UserMapper userMapper;
    @Autowired private CreditLogMapper creditLogMapper;

    /**
     * 评估用户风险等级
     * @return LOW(正常), MEDIUM(关注), HIGH(高危)
     */
    public String evaluateUserRisk(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return "UNKNOWN";

        // 规则1：信用分过低
        if (user.getPoints() < 60) return "HIGH";

        // 规则2：近期频繁扣分 (模拟: 查询最近5条记录是否有3条是扣分)
        List<CreditLog> logs = creditLogMapper.selectList(new QueryWrapper<CreditLog>()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .last("LIMIT 5"));

        long negativeCount = logs.stream()
                .filter(log -> log.getScore() < 0) // 假设扣分记录 score < 0
                .count();

        if (negativeCount >= 3) return "MEDIUM";

        return "LOW";
    }

    /**
     * 检查是否允许预约 (高危用户禁止预约)
     */
    public boolean isBookingAllowed(Long userId) {
        String riskLevel = evaluateUserRisk(userId);
        return !"HIGH".equals(riskLevel);
    }
}