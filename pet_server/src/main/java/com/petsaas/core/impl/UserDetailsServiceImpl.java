package com.petsaas.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security 核心接口实现
 * 用于根据用户名从数据库加载用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户 (这里假设 login_name 是登录账号，也可以是 phone 或 student_id)
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", username)
                .or()
                .eq("phone", username));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 3. 构建 Security User 对象
        // 实际项目中，authorities 应该从数据库的角色表加载，这里简化为默认角色
        String role = user.getIsAdmin() == 1 ? "ROLE_ADMIN" : "ROLE_USER";

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // 数据库中加密后的密码
                Collections.singletonList(() -> role)
        );
    }
}