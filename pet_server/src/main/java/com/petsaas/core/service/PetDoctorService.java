package com.petsaas.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.core.entity.PetDoctor;

import java.util.List;

/**
 * <p>
 * 宠物医生服务接口
 * </p>
 *
 * @author Petsaas
 * @since 2025-12-25
 */
public interface PetDoctorService extends IService<PetDoctor> {

    /**
     * 获取所有在职医生列表
     * @return 医生列表
     */
    List<PetDoctor> getActiveDoctors();
}