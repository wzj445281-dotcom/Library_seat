package com.petsaas.common.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_pet")
public class Pet extends BaseEntity {
    private Long userId;
    private String nickname;
    private Integer type; // 1-狗 2-猫 3-异宠
    private String breed;
    private Integer gender; // 0-MM 1-GG
    private LocalDate birthDate;
    private BigDecimal weight;
    private Integer sterilized;
    private String vaccineStatus;
    private String avatarUrl;
}