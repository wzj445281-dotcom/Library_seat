package com.example.zhizuo.core.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users")
public class User {
    private Long id;
    private String studentId;
    private String name;
    private Integer creditScore;
}