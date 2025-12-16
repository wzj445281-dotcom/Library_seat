package com.example.zhizuo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.entity.Seat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {
    // 继承 BaseMapper 后，自带 selectList 等方法，无需手动写 selectAll
}