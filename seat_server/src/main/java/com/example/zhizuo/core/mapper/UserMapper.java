package com.example.zhizuo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.core.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 基础增删改查
    int insert(User user);
    int updateById(User user);
    User selectById(Long id);

    // 改动：学号 -> 用户名
    User findByUsername(@Param("username") String username);

    List<User> selectAll();
}
