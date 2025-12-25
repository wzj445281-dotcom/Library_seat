package com.example.zhizuo.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service("customUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String studentId) throws UsernameNotFoundException {
        // 1. 查询数据库中的用户
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + studentId);
        }

        // 2. 返回 Spring Security 需要的 UserDetails 对象
        // 这里暂时不处理复杂的角色权限，权限列表传空 ArrayList
        return new org.springframework.security.core.userdetails.User(
                user.getStudentId(),
                user.getPassword(), // 注意：这里应该是加密后的密码
                new ArrayList<>()
        );
    }
}