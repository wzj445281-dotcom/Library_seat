package com.petsaas.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.UserMapper;
import com.petsaas.core.service.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserServiceImpl extends ServiceImpl<UserMapper, User> implements AdminUserService {

    @Override
    public Page<User> getUserList(int page, int size, String username) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> query = new QueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            query.like("username", username);
        }
        // 按积分排序，方便管理员先处理违纪学生
        query.orderByAsc("points");

        return this.page(pageParam, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetCredit(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存�?);
        }

        if (user.getPoints() == 100) {
            throw new RuntimeException("该用户积分已是满分，无需重置");
        }

        user.setPoints(100);
        this.updateById(user);

        // 💡 扩展点：未来可以在这里插入一�?System Log (系统日志�?，记录是哪个管理员操作的
    }
}