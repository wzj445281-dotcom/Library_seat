package com.petsaas.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 宠物医生实体类
 * (补全缺失的实体文件)
 * </p>
 *
 * @author Petsaas
 * @since 2025-12-25
 */
@Data
@TableName("pet_doctor")
public class PetDoctor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 医生姓名
     */
    private String name;

    /**
     * 职称/职位
     */
    private String title;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 擅长领域 (逗号分隔)
     */
    private String specialty;

    /**
     * 从业年限
     */
    private Integer experienceYears;

    /**
     * 简介
     */
    private String description;

    /**
     * 状态 0:休息 1:在职/可预约
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}