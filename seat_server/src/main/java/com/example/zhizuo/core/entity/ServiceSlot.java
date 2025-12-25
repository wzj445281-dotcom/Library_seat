package com.example.zhizuo.core.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceSlot {
    private Long id;

    // 座位名称/标签
    private String name;  // 例如："A-01"、"靠窗-01"

    // 座位类型/区域
    private String type;  // 枚举值：WINDOW(靠窗), QUIET(安静区), DISCUSSION(讨论区), REGULAR(普通区)

    private Integer status; // 1=可用, 0=维护中

    // 新增：基础服务费
    private BigDecimal basePrice;

    private String description;
}
