package com.petsaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.core.entity.TransactionFlow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper extends BaseMapper<TransactionFlow> {
}