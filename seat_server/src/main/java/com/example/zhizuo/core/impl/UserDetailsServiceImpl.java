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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询数据库中的用户
        QueryWrapper<User> query = new QueryWrapper<>();
        // 支持通过 username 或 phone 查询
        query.and(wrapper -> wrapper.eq("username", username).or().eq("phone", username));
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 2. 直接返回 User 实体对象，而不是 Spring Security 的 User
        // 这样 SecurityUtils.getUserId() 就能直接获取到 User 对象和 ID
        // 注意：User 实体需要实现 UserDetails 接口，或者我们创建一个包装类
        // 临时方案：创建一个实现了 UserDetails 的包装类
        return new UserDetailsWrapper(user);
    }

    /**
     * UserDetails 包装类，将 User 实体包装为 UserDetails
     */
    public static class UserDetailsWrapper implements UserDetails {
        private final User user;

        public UserDetailsWrapper(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return new ArrayList<>();
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername() != null ? user.getUsername() : user.getPhone();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}