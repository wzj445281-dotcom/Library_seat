package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.JwtUtil;
import com.example.zhizuo.core.dto.UserLoginDTO;
import com.example.zhizuo.core.dto.UserRegisterDTO;
import com.example.zhizuo.core.entity.CreditLog;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.CreditLogMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "App-认证模块")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CreditLogMapper creditLogMapper; // 新增注入

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          CreditLogMapper creditLogMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.creditLogMapper = creditLogMapper;
    }

    @Operation(summary = "学生登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody @Validated UserLoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getStudentId(), loginDTO.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ApiResponse.error(401, "学号或密码错误");
        }

        String token = jwtUtil.generateToken(loginDTO.getStudentId());

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", loginDTO.getStudentId());
        User user = userMapper.selectOne(query);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("name", user.getName());

        return ApiResponse.success(result);
    }

    @Operation(summary = "学生注册")
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class) // 开启事务，保证两张表同时成功
    public ApiResponse<String> register(@RequestBody @Validated UserRegisterDTO registerDTO) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", registerDTO.getStudentId());
        if (userMapper.selectCount(query) > 0) {
            return ApiResponse.error(400, "该学号已注册");
        }

        // 1. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getStudentId());
        user.setName(registerDTO.getName());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setCreditScore(100);
        userMapper.insert(user);

        // 2. 记录初始信用日志 (需要在 insert user 后，这样才有 user.id)
        CreditLog log = new CreditLog();
        log.setUserId(user.getId());
        log.setType("ADD");
        log.setScore(100);
        log.setReason("新用户注册奖励");
        log.setCreateTime(LocalDateTime.now());
        creditLogMapper.insert(log);

        return ApiResponse.success("注册成功");
    }
}