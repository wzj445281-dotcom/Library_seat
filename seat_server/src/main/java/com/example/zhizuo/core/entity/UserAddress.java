package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户收货地址实体
 */
@Data
@TableName("user_address")
public class UserAddress implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String name;

    /**
     * 收货人电话
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detail;

    /**
     * 是否默认地址
     */
    private Boolean isDefault;

    /**
     * 纬度（用于地图定位）
     */
    private BigDecimal latitude;

    /**
     * 经度（用于地图定位）
     */
    private BigDecimal longitude;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取完整地址（省市区+详细地址）
     */
    public String getFullAddress() {
        return (province != null ? province : "") +
               (city != null ? city : "") +
               (district != null ? district : "") +
               (detail != null ? detail : "");
    }
}

