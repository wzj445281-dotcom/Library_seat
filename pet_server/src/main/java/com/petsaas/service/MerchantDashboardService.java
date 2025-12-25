package com.petsaas.service;

import com.petsaas.vo.DashboardVo;

public interface MerchantDashboardService {
    /**
     * 获取今日经营数据统计
     */
    DashboardVo getTodayStats();
}