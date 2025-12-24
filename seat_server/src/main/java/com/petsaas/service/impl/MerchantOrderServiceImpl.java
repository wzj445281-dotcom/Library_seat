package com.petsaas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.common.domain.Order;
import com.petsaas.mapper.OrderMapper;
import com.petsaas.service.MerchantOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MerchantOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements MerchantOrderService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyOrder(Long merchantId, String verifyCode) {
        // 1. 查询订单
        // 必须校验 merchantId，防止 A店 的人核销了 B店 的订单！
        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getVerifyCode, verifyCode)
                .eq(Order::getMerchantId, merchantId)); // 关键安全校验

        if (order == null) {
            throw new RuntimeException("核销码无效或非本店订单");
        }

        // 2. 校验状态
        if (order.getOrderStatus() == 1) { // 1 代表已完成
            throw new RuntimeException("该订单已核销，请勿重复操作");
        }
        if (order.getOrderStatus() == 2) { // 2 代表已取消
            throw new RuntimeException("订单已取消，无法核销");
        }

        // 3. 执行核销
        order.setOrderStatus(1); // 变更为"已完成"
        // 这里应该更新updatedAt字段，但BaseEntity中没有，所以暂时不设置
        // order.setUpdatedAt(LocalDateTime.now());
        this.updateById(order);
        
        // 4. (可选) 触发后续动作：增加宠物积分、发送微信服务完成通知
        // TODO: 可以在这里添加积分增加逻辑
    }
}