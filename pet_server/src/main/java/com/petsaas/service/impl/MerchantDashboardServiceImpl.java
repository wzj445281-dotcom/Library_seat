package com.petsaas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.core.entity.ProductOrder;
import com.petsaas.core.entity.ServiceBooking;
import com.petsaas.core.mapper.ProductOrderMapper;
import com.petsaas.core.mapper.ServiceBookingMapper;
import com.petsaas.service.MerchantDashboardService;
import com.petsaas.vo.DashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MerchantDashboardServiceImpl implements MerchantDashboardService {

    @Autowired
    private ProductOrderMapper orderMapper;

    @Autowired
    private ServiceBookingMapper bookingMapper;

    @Override
    public DashboardVo getTodayStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        DashboardVo vo = new DashboardVo();

        // 1. 今日商品订单数
        QueryWrapper<ProductOrder> orderQ = new QueryWrapper<>();
        orderQ.ge("create_time", startOfDay);
        vo.setTodayOrderCount(orderMapper.selectCount(orderQ));

        // 2. 今日营收 (简单累加商品金额)
        List<ProductOrder> orders = orderMapper.selectList(orderQ);
        BigDecimal productRevenue = orders.stream()
                .map(ProductOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayRevenue(productRevenue);

        // 3. 待核销服务数
        QueryWrapper<ServiceBooking> bookingQ = new QueryWrapper<>();
        bookingQ.eq("status", "CONFIRMED");
        vo.setPendingVerifyCount(bookingMapper.selectCount(bookingQ));

        // 4. Mock 热销商品 (实际需聚合查询)
        vo.setTopProducts(List.of(
                Map.of("name", "皇家狗粮", "count", 15),
                Map.of("name", "宠物美容", "count", 12),
                Map.of("name", "驱虫药", "count", 8)
        ));

        return vo;
    }
}