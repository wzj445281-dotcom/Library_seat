package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.User;

public interface AdminUserService extends IService<User> {

    /**
     * 分页查询用户列表
     * @param page 当前页
     * @param size 每页条数
     * @param studentId 学号 (可选，模糊查询)
     * @return 分页结果
     */
    Page<User> getUserList(int page, int size, String studentId);

    /**
     * 重置用户信用分
     * @param userId 用户ID
     */
    void resetCredit(Long userId);
}