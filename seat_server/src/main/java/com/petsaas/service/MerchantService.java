package com.petsaas.service;

import com.petsaas.vo.MerchantVo;
import java.math.BigDecimal;
import java.util.List;

public interface MerchantService {
    /**
     * 查询附近的商家
     * @param userLat 用户纬度
     * @param userLng 用户经度
     * @return 按距离排序的商家列表
     */
    List<MerchantVo> selectNearby(BigDecimal userLat, BigDecimal userLng);
}