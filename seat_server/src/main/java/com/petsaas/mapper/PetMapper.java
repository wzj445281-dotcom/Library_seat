package com.petsaas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.common.domain.Pet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}