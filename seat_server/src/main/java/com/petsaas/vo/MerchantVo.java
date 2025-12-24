package com.petsaas.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class MerchantVo implements Serializable {
    private Long id;
    private String name;
    private String address;
    private String logoUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    // 距离字段，用于接收SQL计算出的距离(单位:千米)
    private Double distanceKM;
    
    private Integer status;
    private String phone;
    private String description;
    private String businessHours;
}