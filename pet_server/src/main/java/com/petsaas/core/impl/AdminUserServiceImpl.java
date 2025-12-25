package com.petsaas.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.UserMapper;
import com.petsaas.core.service.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminUserServiceImpl extends ServiceImpl<UserMapper, User> implements AdminUserService {

    @Override
    public IPage<User> getUserList(Page<User> page, String keyword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 动态拼接查询条件
        if (StringUtils.hasText(keyword)) {
            wrapper.like("username", keyword)
                    .or()
                    .like("phone", keyword)
                    .or()
                    .like("student_id", keyword);
        }

        // 按注册时间倒序
        wrapper.orderByDesc("create_time");

        // 排除密码字段，保护隐私
        wrapper.select(User.class, info -> !info.getColumn().equals("password"));

        return this.page(page, wrapper);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 0: 禁用, 1: 正常
        user.setStatus(status);
        this.updateById(user);
    }
}