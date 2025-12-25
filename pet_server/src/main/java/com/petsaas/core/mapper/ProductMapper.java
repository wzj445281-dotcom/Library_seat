package com.petsaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.core.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    // 乐观锁扣减库存：只有库存足够时才扣减
    @Update("UPDATE sys_product SET stock = stock - #{num} WHERE id = #{id} AND stock >= #{num}")
    int deductStock(@Param("id") Long id, @Param("num") Integer num);
}