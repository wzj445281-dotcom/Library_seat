package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("user")
public class User {
    private Long id;
    private String username; // Replaces studentId
    private String phone;
    private String name;
    private String password;
    private Integer points; // Replaces creditScore
    private String role;
    private BigDecimal balance;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    // 添加缺失的方法
    public String getStudentId() {
        return username;
    }
    
    public void setStudentId(String studentId) {
        this.username = studentId;
    }
    
    public int getCreditScore() {
        return points != null ? points : 0;
    }
    
    public void setCreditScore(int creditScore) {
        this.points = creditScore;
    }
}