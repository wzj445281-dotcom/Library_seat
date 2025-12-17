package com.example.zhizuo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.core.entity.SeatHeatStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatHeatStatsMapper extends BaseMapper<SeatHeatStats> {
    // MyBatis Plus 提供基础 CRUD，足够用了
}