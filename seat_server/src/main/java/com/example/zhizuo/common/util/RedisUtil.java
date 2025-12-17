package com.example.zhizuo.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 写入缓存
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 写入缓存并设置过期时间
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 读取缓存
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除缓存
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // 是否存在
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}