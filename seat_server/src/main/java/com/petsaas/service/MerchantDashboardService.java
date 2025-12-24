package com.petsaas.service;

import com.petsaas.vo.DashboardVo;

public interface MerchantDashboardService {
    /**
     * 获取商家仪表盘数据
     * @param merchantId 商户ID
     * @return 仪表盘数据
     */
    DashboardVo getDashboardData(Long merchantId);
}