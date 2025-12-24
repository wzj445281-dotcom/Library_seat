package com.example.zhizuo.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.core.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 扣减库存
     * @param productId 商品ID
     * @param quantity 数量
     * @return 影响行数
     */
    @Update("UPDATE products SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int deductStock(Long productId, Integer quantity);
}