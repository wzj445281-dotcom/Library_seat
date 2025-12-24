package com.example.zhizuo.core.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users")
public class User {
    private Long id;
    private String studentId;
    private String name;
    private String password; // 补全缺失的密码字段
    private Integer points; // 会员积分
}