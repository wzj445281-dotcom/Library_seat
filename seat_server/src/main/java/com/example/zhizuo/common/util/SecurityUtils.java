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

            // 情况2: 如果是 UserDetailsWrapper (我们自定义的包装类)
            if (principal instanceof com.example.zhizuo.core.impl.UserDetailsServiceImpl.UserDetailsWrapper) {
                com.example.zhizuo.core.impl.UserDetailsServiceImpl.UserDetailsWrapper wrapper = 
                    (com.example.zhizuo.core.impl.UserDetailsServiceImpl.UserDetailsWrapper) principal;
                return wrapper.getUser().getId();
            }

            // 情况3: 如果是 Spring Security 的 UserDetails (org.springframework.security.core.userdetails.User)
            // 这种情况不应该出现，因为我们已经修改为返回 UserDetailsWrapper
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                throw new RuntimeException("当前用户信息类型不支持直接获取ID，请通过 username 查询: " + username);
            }

            // 情况4: 如果是 String (可能是 username)
            if (principal instanceof String) {
                String username = (String) principal;
                throw new RuntimeException("当前用户信息为 String 类型(username)，无法直接获取ID。请修改 UserDetailsServiceImpl 返回 User 实体");
            }

            // 情况4: 尝试通过反射获取 getId 方法 (兼容其他 UserDetails 实现)
            try {
                java.lang.reflect.Method getId = principal.getClass().getMethod("getId");
                return (Long) getId.invoke(principal);
            } catch (Exception ex) {
                // ignore
            }

            // 情况5: 如果是 Map (某些 JWT 解析库会将 Payload 转为 Map)
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
     * 获取当前登录用户ID (String类型)
     * 用于收藏功能
     */
    public static String getCurrentUserId() {
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