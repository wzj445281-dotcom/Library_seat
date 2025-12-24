package com.petsaas.common.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_merchant")
public class Merchant extends BaseEntity {
    private String name;
    private String address;
    private BigDecimal longitude; // 经度
    private BigDecimal latitude;  // 纬度
    private String contactPhone;
    private String logoUrl;
    private Integer status; // 1-营业中
    private String description;
}