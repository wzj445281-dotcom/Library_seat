package com.example.zhizuo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis Plus 会自动实现基础 CRUD，无需手动写 SQL
}