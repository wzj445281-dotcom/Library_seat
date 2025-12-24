package com.petsaas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petsaas.common.domain.Merchant;
import com.petsaas.vo.MerchantVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    /**
     * 查询附近的商家，按距离排序
     * @param userLat 用户纬度
     * @param userLng 用户经度
     * @return 商家列表
     */
    List<MerchantVo> selectNearby(@Param("userLat") BigDecimal userLat, @Param("userLng") BigDecimal userLng);
}