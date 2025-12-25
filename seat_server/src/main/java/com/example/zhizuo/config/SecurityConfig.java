package com.example.zhizuo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Spring Security 安全配置类
 * 修复 AuthenticationManager 注入失败问题
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    /**
     * 显式注入 AuthenticationManager
     * 解决 AuthController 构造函数注入失败的问题
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF
                .csrf().disable()

                // 2. 禁用 Session (使用 JWT)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 3. 配置路径权限
                .authorizeRequests()
                // 登录、商品、AI 等基础接口允许匿名访问
                .antMatchers("/api/app/auth/**", "/api/app/product/**", "/api/app/ai/**").permitAll()
                // 静态资源、Swagger 文档允许匿名访问
                .antMatchers("/", "/index.html", "/static/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 其他接口需要认证
                .antMatchers("/api/app/**").authenticated()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();

        // 4. 添加 JWT 过滤器
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}