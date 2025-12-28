package com.example.zhizuo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.zhizuo.config.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 解决小程序 POST 403 问题
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 前后端分离通常无状态
                .and()
                .authorizeRequests()
                // 放行静态资源：HTML页面、API文档、静态文件
                .antMatchers("/**/*.html", "/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                // 放行 WebSocket 连接
                .antMatchers("/ws/**", "/app/**", "/topic/**", "/queue/**").permitAll()
                // 放行所有API接口（开发阶段，生产环境需要更严格的配置）
                .antMatchers("/api/**").permitAll()
                // 放行白名单：登录、商品流、AI接口
                .antMatchers("/api/app/auth/**", "/api/app/product/**", "/api/app/ai/**").permitAll()
                .anyRequest().permitAll(); // 开发阶段全部放行，但JWT Filter仍会处理Token

        // 确保 JWT 过滤器生效，在所有请求之前处理Token
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * === 核心修复 ===
     * 补充 PasswordEncoder Bean，解决 AuthController 依赖报错
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}