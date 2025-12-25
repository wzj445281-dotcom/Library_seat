package com.petsaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.Product;
import com.petsaas.core.entity.ProductOrder;
import com.petsaas.core.entity.TransactionFlow;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.ProductMapper;
import com.petsaas.core.mapper.ProductOrderMapper; // 确保引用的是这个
import com.petsaas.core.mapper.TransactionMapper;
import com.petsaas.core.mapper.UserMapper;
import com.petsaas.core.service.ProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {

    @Autowired private ProductMapper productMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private TransactionMapper transactionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrder createOrder(Long userId, Long productId, Integer quantity) {
        // 1. 校验商品
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        // 2. 扣减库存 (MyBatis乐观锁 Update 语句)
        int rows = productMapper.deductStock(productId, quantity);
        if (rows == 0) {
            throw new RuntimeException("商品太火爆了，手慢无！(库存扣减失败)");
        }

        // 3. 创建订单
        ProductOrder order = new ProductOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        // 如果是多商户，这里应设置 merchantId: order.setMerchantId(product.getMerchantId());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus("PENDING"); // 待支付
        order.setCreateTime(LocalDateTime.now());

        this.save(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, String orderNo) {
        ProductOrder order = this.getOne(new QueryWrapper<ProductOrder>().eq("order_no", orderNo));
        if (order == null) throw new RuntimeException("订单不存在");
        if (!"PENDING".equals(order.getStatus())) throw new RuntimeException("订单状态异常");

        User user = userMapper.selectById(userId);
        if (user.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }

        // 1. 扣款
        userMapper.deductBalance(userId, order.getTotalAmount());

        // 2. 更新订单
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);

        // 3. 记录流水
        TransactionFlow flow = new TransactionFlow();
        flow.setUserId(userId);
        flow.setAmount(order.getTotalAmount().negate());
        flow.setType("PAYMENT");
        flow.setDescription("购买商品消费");
        flow.setOrderNo(orderNo);
        flow.setCreateTime(LocalDateTime.now());
        transactionMapper.insert(flow);
    }

    @Override
    public IPage<ProductOrder> getMyOrders(Page<ProductOrder> page, Long userId, String status) {
        QueryWrapper<ProductOrder> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");
        return this.page(page, query);
    }
}