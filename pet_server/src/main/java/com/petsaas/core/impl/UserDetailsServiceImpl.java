package com.petsaas.core.impl; 
 
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; 
import com.petsaas.core.entity.User; 
import com.petsaas.core.mapper.UserMapper; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.core.GrantedAuthority; 
import org.springframework.security.core.authority.SimpleGrantedAuthority; 
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.security.core.userdetails.UserDetailsService; 
import org.springframework.security.core.userdetails.UsernameNotFoundException; 
import org.springframework.stereotype.Service; 
 
import java.util.ArrayList; 
import java.util.List; 
 
/** 
 * Spring Security 用户认证逻辑 
 * 适配 Pet Mall �?Users �?
 */ 
@Service 
public class UserDetailsServiceImpl implements UserDetailsService { 
 
    @Autowired 
    private UserMapper userMapper; 
 
    @Override 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { 
        // 1. 根据用户名查询用�?
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(); 
        queryWrapper.eq("username", username); 
        User user = userMapper.selectOne(queryWrapper); 
 
        if (user == null) { 
            throw new UsernameNotFoundException("用户不存�? " + username); 
        } 
 
        // 2. 构建权限列表 (ROLE_USER, ROLE_ADMIN, ROLE_DOCTOR) 
        List<GrantedAuthority> authorities = new ArrayList<>(); 
        if (user.getRole() != null) { 
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole())); 
        } else { 
            authorities.add(new SimpleGrantedAuthority("ROLE_USER")); 
        } 
 
        // 3. 返回 Spring Security 需要的 UserDetails 对象 
        return new org.springframework.security.core.userdetails.User( 
                user.getUsername(), 
                user.getPassword(), 
                authorities 
        ); 
    } 
}