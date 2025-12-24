package com.example.zhizuo.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 * 负责统一获取当前登录用户的身份信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的学号 (StudentId)
     * 基于 JWT Filter 解析后的 Principal
     * @return studentId
     * @throws RuntimeException 如果未获取到认证信息
     */
    public static String getCurrentStudentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            // 这里抛出的异常会被 GlobalExceptionHandler 捕获并返回 401
            throw new RuntimeException("用户未登录或身份已过期");
        }
        return (String) auth.getPrincipal();
    }

    /**
     * 获取当前登录用户的ID
     * @return userId
     */
    public static Long getCurrentUserId() {
        // 临时实现，实际应该从数据库查询或JWT中获取
        // 这里简化处理，假设用户名就是ID的字符串形式
        String username = getCurrentStudentId();
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new RuntimeException("无法解析用户ID");
        }
    }
}