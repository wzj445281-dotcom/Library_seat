package com.petsaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.dto.OrderCreateDTO;
import com.petsaas.core.entity.Product;
import com.petsaas.core.entity.ProductOrder;
import com.petsaas.core.mapper.ProductMapper;
import com.petsaas.core.mapper.ProductOrderMapper;
import com.petsaas.core.service.ProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 校验商品和库存
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStock() < dto.getCount()) {
            throw new RuntimeException("库存不足");
        }

        // 2. 扣减库存 (简单扣减，生产环境建议用乐观锁或Redis Lua脚本)
        product.setStock(product.getStock() - dto.getCount());
        product.setSales(product.getSales() + dto.getCount());
        productMapper.updateById(product);

        // 3. 生成订单号
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000));

        // 4. 创建订单
        ProductOrder order = new ProductOrder();
        order.setUserId(userId);
        order.setProductId(dto.getProductId());
        order.setProductSnapshot(product.getName()); // 保存快照
        order.setOrderNo(orderNo);
        order.setCount(dto.getCount());
        order.setAmount(dto.getAmount());
        order.setStatus("unpaid"); // 待支付
        order.setCreateTime(LocalDateTime.now());

        this.save(order);

        return orderNo;
    }

    @Override
    public List<ProductOrder> getUserOrders(Long userId, String status) {
        QueryWrapper<ProductOrder> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");
        return this.list(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId) {
        ProductOrder order = this.getById(orderId);
        if (order == null || !"unpaid".equals(order.getStatus())) {
            return false;
        }
        // 模拟支付成功
        order.setStatus("paid");
        order.setPayTime(LocalDateTime.now());
        return this.updateById(order);
    }
}