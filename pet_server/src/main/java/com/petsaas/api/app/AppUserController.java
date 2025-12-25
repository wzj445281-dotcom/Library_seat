package com.petsaas.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.common.ApiResponse;
import com.petsaas.core.entity.CreditLog;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.CreditLogMapper;
import com.petsaas.core.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/app/user")
@Tag(name = "App-用户模块")
public class AppUserController {

    private final CreditLogMapper creditLogMapper;
    private final UserMapper userMapper;

    public AppUserController(CreditLogMapper creditLogMapper, UserMapper userMapper) {
        this.creditLogMapper = creditLogMapper;
        this.userMapper = userMapper;
    }

    @Operation(summary = "获取当前用户信息(含最新信用分)")
    @GetMapping("/info")
    public ApiResponse<User> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();

        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username);
        User user = userMapper.selectOne(query);

        if (user != null) {
            user.setPassword(null); // 脱敏，不返回密码
        }
        return ApiResponse.success(user);
    }

    @Operation(summary = "获取我的信用分变动日志")
    @GetMapping("/credit-logs")
    public ApiResponse<List<CreditLog>> getMyCreditLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();

        QueryWrapper<CreditLog> query = new QueryWrapper<>();
        query.inSql("user_id", "SELECT id FROM users WHERE username = '" + username + "'");
        query.orderByDesc("create_time");

        return ApiResponse.success(creditLogMapper.selectList(query));
    }
}