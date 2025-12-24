package com.petsaas.service.impl;

import com.petsaas.mapper.OrderMapper;
import com.petsaas.service.MerchantDashboardService;
import com.petsaas.vo.DashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class MerchantDashboardServiceImpl implements MerchantDashboardService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public DashboardVo getDashboardData(Long merchantId) {
        DashboardVo dashboardVo = new DashboardVo();
        
        // 1. 获取今日统计数据
        Map<String, Object> todayStats = orderMapper.getTodayStats(merchantId);
        dashboardVo.setTodayIncome((BigDecimal) todayStats.get("income"));
        dashboardVo.setTodayOrderCount((Integer) todayStats.get("count"));
        
        // 2. 获取累计会员数
        dashboardVo.setTotalMemberCount(orderMapper.getTotalMemberCount(merchantId));
        
        // 3. 获取热销服务Top3
        List<DashboardVo.HotServiceVo> hotServices = orderMapper.getHotServices(merchantId);
        dashboardVo.setHotServices(hotServices);
        
        return dashboardVo;
    }
}