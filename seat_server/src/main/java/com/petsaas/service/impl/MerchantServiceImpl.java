package com.petsaas.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.mapper.MerchantMapper;
import com.petsaas.service.MerchantService;
import com.petsaas.vo.MerchantVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, com.petsaas.common.domain.Merchant> implements MerchantService {
    
    @Override
    public List<MerchantVo> selectNearby(BigDecimal userLat, BigDecimal userLng) {
        return baseMapper.selectNearby(userLat, userLng);
    }
}