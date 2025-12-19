package com.example.zhizuo.core.strategy.impl;

import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.strategy.BenefitStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TimeCardStrategy implements BenefitStrategy {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String getType() {
        return "TIME_CARD";
    }

    /**
     * 这里的逻辑是：用户兑换了"4小时加时卡"
     * 系统不发实体卡，而是直接在 Redis 里给用户打个标
     * 下次预约时，ReservationService 会检查这个标，从而放宽时长限制
     */
    @Override
    public void sendBenefit(Long userId, String value) {
        log.info("用户 {} 兑换了加时权益，时长: {} 小时", userId, value);

        // Key: user:benefit:time_extend:{userId}
        String key = "user:benefit:time_extend:" + userId;

        // 存入 Redis，有效期 24 小时（假设权益即买即用，或者当天有效）
        // Value: 允许额外增加的小时数
        redisTemplate.opsForValue().set(key, Integer.parseInt(value), 24, TimeUnit.HOURS);

        log.info("权益已生效，用户今日预约时长上限已增加");
    }
}