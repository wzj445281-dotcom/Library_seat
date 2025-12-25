package com.petsaas.core.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("users")
public class User {
    private Long id;
    private String username; // 用户�?    private String password; // 密码
    private String role; // 角色：USER/ADMIN/DOCTOR
    private String name; // 真实姓名
    private String phone; // 手机号
    private BigDecimal balance; // 账户余额
    private Integer points; // 会员积分
    private LocalDateTime createTime; // 创建时间

    // 手动添加getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    // 为了兼容性，添加getCreditScore和setCreditScore方法
    public Integer getCreditScore() {
        return points;
    }
    
    public void setCreditScore(int creditScore) {
        this.points = creditScore;
    }
}