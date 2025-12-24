package com.petsaas.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.common.domain.Order;
import com.petsaas.common.domain.ServiceSchedule;
import com.petsaas.dto.CreateOrderReq;
import com.petsaas.mapper.OrderMapper;
import com.petsaas.mapper.ServiceScheduleMapper;
import com.petsaas.service.OrderService;
import com.petsaas.service.ServiceScheduleService;
import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ServiceScheduleService scheduleService;
    
    @Autowired
    private ServiceScheduleMapper scheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CreateOrderReq req) {
        // 1. 校验排期是否存在
        ServiceSchedule schedule = scheduleMapper.selectById(req.getScheduleId());
        if (schedule == null || schedule.getStatus() == 0) {
            throw new RuntimeException("该时段不可预约");
        }

        // 2. 核心：扣减库存 (利用 SQL 原子性防止超卖)
        // SQL: UPDATE sys_service_schedule SET booked_count = booked_count + 1
        //      WHERE id = ? AND booked_count < max_capacity
        boolean updateSuccess = scheduleService.lambdaUpdate()
                .setSql("booked_count = booked_count + 1")
                .eq(ServiceSchedule::getId, req.getScheduleId())
                .lt(ServiceSchedule::getBookedCount, schedule.getMaxCapacity()) // 关键条件！
                .update();

        if (!updateSuccess) {
            throw new RuntimeException("手慢了，该时段已约满！");
        }

        // 3. 创建订单
        Order order = new Order();
        order.setOrderNo(System.currentTimeMillis() + ""); // 简单生成订单号，实际项目应使用雪花算法
        order.setUserId(1L); // TODO: 从SecurityContext获取当前用户ID
        order.setMerchantId(schedule.getMerchantId()); // 继承排期的商户ID
        order.setOrderType(1); // 服务预约
        order.setPetId(req.getPetId());
        order.setScheduleId(schedule.getId());
        order.setTotalAmount(req.getPrice());
        order.setOrderStatus(0); // 待支付
        
        // 生成8位核销码
        String verifyCode = RandomUtil.randomNumbers(8);
        order.setVerifyCode(verifyCode);
        
        this.save(order);
        
        return order.getId();
    }
}