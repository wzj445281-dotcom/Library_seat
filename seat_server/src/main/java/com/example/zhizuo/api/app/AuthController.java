package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.JwtUtil;
import com.example.zhizuo.core.dto.UserLoginDTO;
import com.example.zhizuo.core.dto.UserRegisterDTO;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "学生登录")
    @PostMapping("/login")
    // 使用 @RequestBody 接收 JSON，@Validated 开启校验
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
        query.eq("student_id", loginDTO.getStudentId());
        User user = userMapper.selectOne(query);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("name", user.getName());

        return ApiResponse.success(result);
    }

    @Operation(summary = "学生注册")
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Validated UserRegisterDTO registerDTO) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("student_id", registerDTO.getStudentId());
        if (userMapper.selectCount(query) > 0) {
            return ApiResponse.error(400, "该学号已注册");
        }

        User user = new User();
        user.setStudentId(registerDTO.getStudentId());
        user.setName(registerDTO.getName());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setCreditScore(100);

        userMapper.insert(user);
        return ApiResponse.success("注册成功");
    }
}