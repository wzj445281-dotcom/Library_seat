package com.petsaas.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullExpression;
import net.sf.jsqlparser.expression.Expression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class MybatisPlusConfig {

    // 注意：实际项目中应该通过依赖注入获取HttpServletRequest
    // 这里为了简化示例，暂时使用静态方法获取
    private static HttpServletRequest request;

    public static void setRequest(HttpServletRequest request) {
        MybatisPlusConfig.request = request;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加多租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            
            // 1. 获取当前租户(商户)ID
            @Override
            public Expression getTenantId() {
                // 实际项目中，这里应该从 SecurityContext 或 ThreadLocal 中获取
                // 假设前端Header传来: X-Merchant-Id
                if (request != null) {
                    String merchantId = request.getHeader("X-Merchant-Id");
                    if (merchantId != null) {
                        return new LongValue(Long.parseLong(merchantId));
                    }
                }
                
                // 如果是 C端用户浏览，或者管理员后台，可能不需要限制，返回 NullExpression
                // 或者返回一个默认值
                return new NullExpression();
            }

            // 2. 指定数据库中的字段名
            @Override
            public String getTenantIdColumn() {
                return "merchant_id";
            }

            // 3. 哪些表不需要隔离？(白名单)
            @Override
            public boolean ignoreTable(String tableName) {
                // sys_merchant 本身不需要隔离 (平台要看所有店)
                // sys_user 用户表也是全局的
                // sys_pet 宠物属于用户，不属于店
                return "sys_merchant".equalsIgnoreCase(tableName) || 
                       "sys_user".equalsIgnoreCase(tableName) || 
                       "sys_pet".equalsIgnoreCase(tableName);
            }
        }));

        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}