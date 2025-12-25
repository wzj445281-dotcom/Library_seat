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
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 如有 JwtFilter 请取消注释并注入

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /* * 如果你有 JwtFilter，请在此处注入
     * private final JwtFilter jwtFilter;
     * public SecurityConfig(JwtFilter jwtFilter) { this.jwtFilter = jwtFilter; }
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 解决小程序 POST 403 问题
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 前后端分离通常无状态
                .and()
                .authorizeRequests()
                // 放行白名单：登录、商品流、AI接口
                .antMatchers("/api/app/auth/**", "/api/app/product/**", "/api/app/ai/**").permitAll()
                .anyRequest().authenticated();

        // http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // 确保 JWT 过滤器生效

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