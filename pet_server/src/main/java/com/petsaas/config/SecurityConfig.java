package com.petsaas.config;

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
                .csrf().disable() // 禁用 CSRF，因为使�?JWT
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 禁用 Session
                .and()
                .authorizeRequests()
                // --- 🔧 核心修复开始：放行 Knife4j/Swagger 文档资源 ---
                .antMatchers("/doc.html").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v3/api-docs/**").permitAll()

                // --- 放行静态资�?(监控大屏�? ---
                .antMatchers("/monitor.html", "/admin.html", "/feedback_manage.html", "/order_manage.html", "/resource_manage.html").permitAll()
                .antMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()

                // --- 放行认证接口 (登录/注册) ---
                .antMatchers("/api/app/auth/**").permitAll()
                .antMatchers("/api/admin/auth/**").permitAll()

                // --- 其他接口需认证 ---
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}