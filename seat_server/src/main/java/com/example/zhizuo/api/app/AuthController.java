package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.JwtUtil;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.UserMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 登录接口
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String studentId = loginRequest.get("studentId");
        String password = loginRequest.get("password");

        try {
            // 1. 调用 Spring Security 进行认证
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(studentId, password)
            );
        } catch (BadCredentialsException e) {
            return ApiResponse.error(401, "学号或密码错误");
        }

        // 2. 认证成功，生成 Token
        String token = jwtUtil.generateToken(studentId);

        // 3. 查一下用户ID方便前端用
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        User user = userMapper.selectOne(query);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("name", user.getName());

        return ApiResponse.success(result);
    }

    /**
     * 注册接口 (用于测试，生成加密密码)
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody User user) {
        // 检查学号是否已存在
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("student_id", user.getStudentId());
        if (userMapper.selectCount(query) > 0) {
            return ApiResponse.error(400, "该学号已注册");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreditScore(100); // 初始信用分

        userMapper.insert(user);
        return ApiResponse.success("注册成功");
    }
}