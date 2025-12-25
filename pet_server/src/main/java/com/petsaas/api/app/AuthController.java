package com.petsaas.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.common.ApiResponse;
import com.petsaas.common.util.JwtUtil;
import com.petsaas.core.dto.UserLoginDTO;
import com.petsaas.core.dto.UserRegisterDTO;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse login(@RequestBody UserLoginDTO loginDTO) {
        // 使用 username 查询 (也叫studentId)
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", loginDTO.getUsername());
        User user = userMapper.selectOne(query);

        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ApiResponse.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return ApiResponse.success(data);
    }

    @PostMapping("/register")
    public ApiResponse register(@RequestBody UserRegisterDTO registerDTO) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", registerDTO.getUsername());
        if (userMapper.selectCount(query) > 0) {
            return ApiResponse.error("用户已存在");
        }

        User user = new User();
        // 字段适配: setUsername 替代 setStudentId
        user.setUsername(registerDTO.getUsername());
        user.setName(registerDTO.getName());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        
        // 字段适配: setPoints 替代 setCreditScore
        user.setPoints(100); // 初始积分
        user.setBalance(new BigDecimal("0.00")); // 初始余额
        user.setRole("USER");
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        return ApiResponse.success("注册成功");
    }
}