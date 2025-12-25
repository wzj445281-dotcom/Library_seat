package com.example.zhizuo.common.util;

import com.example.zhizuo.core.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 用于获取当前登录用户的信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new RuntimeException("未获取到登录信息");
            }

            Object principal = authentication.getPrincipal();

            // 情况1: Principal 是我们自定义的 User 实体
            // (通常在 UserDetailsServiceImpl 中直接返回了 User 对象)
            if (principal instanceof User) {
                return ((User) principal).getId();
            }

            // 情况2: 尝试通过反射获取 getId 方法 (兼容其他 UserDetails 实现)
            try {
                java.lang.reflect.Method getId = principal.getClass().getMethod("getId");
                return (Long) getId.invoke(principal);
            } catch (Exception ex) {
                // ignore
            }

            // 情况3: 如果是 Map (某些 JWT 解析库会将 Payload 转为 Map)
            if (principal instanceof java.util.Map) {
                Object id = ((java.util.Map<?, ?>) principal).get("id");
                if (id == null) {
                    id = ((java.util.Map<?, ?>) principal).get("userId");
                }
                if (id != null) {
                    return Long.valueOf(id.toString());
                }
            }

            throw new RuntimeException("无法识别的用户信息类型: " + principal.getClass().getName());

        } catch (Exception e) {
            // 在实际生产中，建议记录日志
            throw new RuntimeException("获取当前用户ID失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录学生ID (兼容旧代码调用的别名)
     * 修改为返回 String 以兼容 AppReservationController 中的类型定义
     */
    public static String getCurrentStudentId() {
        return String.valueOf(getUserId());
    }

    /**
     * 获取当前登录用户实体
     */
    public static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}