package com.petsaas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.common.domain.ServiceSchedule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceScheduleMapper extends BaseMapper<ServiceSchedule> {
}