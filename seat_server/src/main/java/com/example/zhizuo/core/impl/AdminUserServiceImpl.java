package com.example.zhizuo.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserServiceImpl extends ServiceImpl<UserMapper, User> implements AdminUserService {

    @Override
    public Page<User> getUserList(int page, int size, String studentId) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> query = new QueryWrapper<>();

        if (studentId != null && !studentId.isEmpty()) {
            query.like("student_id", studentId);
        }
        // 按信用分排序，方便管理员先处理违纪学生
        query.orderByAsc("credit_score");

        return this.page(pageParam, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetCredit(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getCreditScore() == 100) {
            throw new RuntimeException("该用户信用分已是满分，无需重置");
        }

        user.setCreditScore(100);
        this.updateById(user);

        // 💡 扩展点：未来可以在这里插入一条 System Log (系统日志表)，记录是哪个管理员操作的
    }
}