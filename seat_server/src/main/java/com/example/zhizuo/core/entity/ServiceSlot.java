package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@TableName("service_slots") // 对应数据库表名
public class ServiceSlot {
    private Long id;
    private String name;
    private String serviceType; // 例如 "Basic Bath", "Premium Grooming"
    private BigDecimal price; // 服务单价
    private Integer status; // 1可用 0维修

    @TableField(exist = false)
    private Double busyScore;

    // 手动添加getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getBusyScore() {
        return busyScore;
    }

    public void setBusyScore(Double busyScore) {
        this.busyScore = busyScore;
    }
}