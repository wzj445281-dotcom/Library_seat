package com.example.zhizuo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 扣减数据库库存 (乐观锁或直接扣减)
     * 注意：高并发主要靠 Redis 防护，这里是兜底更新
     */
    @Update("UPDATE coupon SET stock = stock - 1 WHERE id = #{id} AND stock > 0")
    int decreaseStock(Long id);
}