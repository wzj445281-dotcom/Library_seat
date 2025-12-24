package com.example.zhizuo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.core.entity.StationBusyStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StationBusyStatsMapper extends BaseMapper<StationBusyStats> {
    // MyBatis Plus 会自动实现基础 CRUD，无需手动写 SQL
}