package com.petsaas.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

public class SecurityUtils {
    
    /**
     * 从请求头获取当前商户ID
     * @return 商户ID
     */
    public static Long getMerchantId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String merchantId = request.getHeader("X-Merchant-Id");
            if (merchantId != null) {
                return Long.parseLong(merchantId);
            }
        }
        
        // 默认返回1L，实际项目中应该抛出异常
        return 1L;
    }
    
    /**
     * 从请求头获取当前用户ID
     * @return 用户ID
     */
    public static Long getUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                return Long.parseLong(userId);
            }
        }
        
        // 默认返回1L，实际项目中应该抛出异常
        return 1L;
    }
}