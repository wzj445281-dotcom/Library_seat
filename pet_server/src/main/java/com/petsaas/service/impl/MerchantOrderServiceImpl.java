package com.petsaas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petsaas.core.entity.ProductOrder;
import com.petsaas.core.entity.ServiceBooking;
import com.petsaas.core.mapper.ProductOrderMapper;
import com.petsaas.core.mapper.ServiceBookingMapper;
import com.petsaas.service.MerchantOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantOrderServiceImpl implements MerchantOrderService {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private ServiceBookingMapper serviceBookingMapper;

    /**
     * 核销逻辑：同时支持 "商品订单" 和 "服务预约" 的核销
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyOrder(String code) {
        // 1. 先尝试查找服务预约 (ServiceBooking) - 假设 code 是 bookingNo
        QueryWrapper<ServiceBooking> bookingQuery = new QueryWrapper<>();
        bookingQuery.eq("booking_no", code);
        ServiceBooking booking = serviceBookingMapper.selectOne(bookingQuery);

        if (booking != null) {
            if ("CONFIRMED".equals(booking.getStatus())) {
                booking.setStatus("COMPLETED");
                booking.setCheckInTime(LocalDateTime.now());
                serviceBookingMapper.updateById(booking);
                return true;
            }
            throw new RuntimeException("预约单状态不可核销: " + booking.getStatus());
        }

        // 2. 如果不是服务预约，尝试查找商品订单 (ProductOrder)
        QueryWrapper<ProductOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("order_no", code);
        ProductOrder order = productOrderMapper.selectOne(orderQuery);

        if (order != null) {
            // 假设 'PAID' 或 'SHIPPED' 状态可以核销
            if ("PAID".equals(order.getStatus()) || "SHIPPED".equals(order.getStatus()) || "UNPAID".equals(order.getStatus())) {
                order.setStatus("COMPLETED");
                order.setCompleteTime(LocalDateTime.now());
                productOrderMapper.updateById(order);
                return true;
            }
            throw new RuntimeException("商品订单状态不可核销: " + order.getStatus());
        }

        throw new RuntimeException("无效的核销码/订单号");
    }

    @Override
    public List<ServiceBooking> getPendingOrders() {
        // 简单返回当天所有未核销的预约
        QueryWrapper<ServiceBooking> query = new QueryWrapper<>();
        query.eq("status", "CONFIRMED")
                .ge("appointment_time", LocalDateTime.now().toLocalDate().atStartOfDay());
        return serviceBookingMapper.selectList(query);
    }
}