package com.petsaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.core.entity.PetDoctor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetDoctorMapper extends BaseMapper<PetDoctor> {
}