package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.JwtUtil;
import com.example.zhizuo.core.dto.UserLoginDTO;
import com.example.zhizuo.core.dto.UserRegisterDTO;
import com.example.zhizuo.core.dto.AccountLoginDTO;
import com.example.zhizuo.core.dto.AccountRegisterDTO;
import com.example.zhizuo.core.entity.CreditLog;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.CreditLogMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "App-认证模块")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CreditLogMapper creditLogMapper;
    private final StringRedisTemplate redisTemplate;

    public AuthController(JwtUtil jwtUtil,
                          UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          CreditLogMapper creditLogMapper,
                          StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.creditLogMapper = creditLogMapper;
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "发送验证码")
    @GetMapping("/send-code")
    public ApiResponse<String> sendCode(@RequestParam String phone) {
        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return ApiResponse.error(400, "手机号格式不正确");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存储到Redis，5分钟过期
        String key = "sms:code:" + phone;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        // 模拟发送短信（实际项目中应该调用短信服务商API）
        System.out.println("【瑞幸咖啡】验证码：" + code + "，5分钟内有效。请勿泄露给他人。");

        return ApiResponse.success("验证码已发送（开发模式：验证码为 " + code + "）");
    }

    @Operation(summary = "用户登录（手机号+密码）")
    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody @Validated UserLoginDTO loginDTO) {
        // 1. 查找用户（通过手机号）
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("phone", loginDTO.getPhone());
        User user = userMapper.selectOne(query);

        if (user == null) {
            return ApiResponse.error(404, "用户不存在，请先注册");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "密码错误");
        }

        // 3. 生成Token（使用手机号作为标识）
        String token = jwtUtil.generateToken(loginDTO.getPhone());

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("name", user.getName());
        result.put("phone", user.getPhone());

        return ApiResponse.success(result);
    }

    @Operation(summary = "用户注册（手机号+姓名+密码）")
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> register(@RequestBody @Validated UserRegisterDTO registerDTO) {
        // 1. 检查手机号是否已注册
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("phone", registerDTO.getPhone());
        if (userMapper.selectCount(query) > 0) {
            return ApiResponse.error(400, "该手机号已注册");
        }

        // 2. 验证密码长度
        if (registerDTO.getPassword().length() < 6) {
            return ApiResponse.error(400, "密码至少6位");
        }

        // 3. 创建用户
        User user = new User();
        user.setPhone(registerDTO.getPhone());
        user.setName(registerDTO.getName());
        user.setUsername(registerDTO.getPhone()); // 使用手机号作为用户名
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 加密存储密码
        user.setCreditScore(100);
        user.setPoints(100);
        userMapper.insert(user);

        // 4. 记录初始信用日志
        CreditLog log = new CreditLog();
        log.setUserId(user.getId());
        log.setType("ADD");
        log.setScore(100);
        log.setReason("新用户注册奖励");
        log.setCreateTime(LocalDateTime.now());
        creditLogMapper.insert(log);

        return ApiResponse.success("注册成功");
    }

    /**
     * 答辩演示专用登录接口 - 绕过微信验证，直接使用ID为1的用户登录
     * ⚠️ 仅用于答辩演示环境，生产环境请删除此接口
     */
    @Operation(summary = "答辩演示专用登录（免验证）")
    @PostMapping("/mock-login")
    public ApiResponse<Map<String, String>> mockLogin() {
        // 直接查询ID为1的用户（管理员或测试账号）
        User user = userMapper.selectById(1L);
        
        if (user == null) {
            return ApiResponse.error(404, "演示账号不存在，请先创建ID为1的用户");
        }

        // 生成Token（使用手机号作为标识，如果手机号为空则使用用户名）
        String identifier = user.getPhone() != null && !user.getPhone().isEmpty() 
            ? user.getPhone() 
            : (user.getUsername() != null ? user.getUsername() : "mock_user_1");
        String token = jwtUtil.generateToken(identifier);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("name", user.getName() != null ? user.getName() : "演示用户");
        result.put("phone", user.getPhone() != null ? user.getPhone() : "");

        return ApiResponse.success(result);
    }

    /**
     * 账号密码注册接口
     * 用于答辩演示环境，替代微信登录
     */
    @Operation(summary = "账号密码注册（用户名+密码+昵称）")
    @PostMapping("/register/account")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> accountRegister(@RequestBody @Validated AccountRegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", registerDTO.getUsername());
        if (userMapper.selectCount(query) > 0) {
            return ApiResponse.error(400, "该用户名已被使用，请更换");
        }

        // 2. 验证密码长度（DTO已校验，这里再次确认）
        if (registerDTO.getPassword().length() < 6) {
            return ApiResponse.error(400, "密码至少6位");
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setName(registerDTO.getNickname()); // 使用昵称作为真实姓名
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 使用BCrypt加密存储密码
        user.setPoints(100); // 初始积分
        user.setCreditScore(100); // 初始信用分
        user.setRole("USER"); // 默认角色
        userMapper.insert(user);

        // 4. 记录初始信用日志
        CreditLog log = new CreditLog();
        log.setUserId(user.getId());
        log.setType("ADD");
        log.setScore(100);
        log.setReason("新用户注册奖励");
        log.setCreateTime(LocalDateTime.now());
        creditLogMapper.insert(log);

        return ApiResponse.success("注册成功，请登录");
    }

    /**
     * 账号密码登录接口
     * 用于答辩演示环境，替代微信登录
     */
    @Operation(summary = "账号密码登录（用户名+密码）")
    @PostMapping("/login/account")
    public ApiResponse<Map<String, String>> accountLogin(@RequestBody @Validated AccountLoginDTO loginDTO) {
        // 1. 查找用户（通过用户名）
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", loginDTO.getUsername());
        User user = userMapper.selectOne(query);

        if (user == null) {
            return ApiResponse.error(404, "用户不存在，请先注册");
        }

        // 2. 验证密码（使用BCrypt验证）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "密码错误");
        }

        // 3. 生成Token（使用用户名作为标识）
        String token = jwtUtil.generateToken(loginDTO.getUsername());

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("name", user.getName() != null ? user.getName() : user.getUsername());
        result.put("username", user.getUsername());
        result.put("phone", user.getPhone() != null ? user.getPhone() : "");

        return ApiResponse.success(result);
    }
}