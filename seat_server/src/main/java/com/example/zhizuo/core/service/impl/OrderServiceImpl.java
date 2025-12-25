package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.IdUtil;
import com.example.zhizuo.core.entity.Order;
import com.example.zhizuo.core.entity.OrderItem;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.entity.UserCoupon;
import com.example.zhizuo.core.mapper.OrderItemMapper;
import com.example.zhizuo.core.mapper.OrderMapper;
import com.example.zhizuo.core.mapper.ProductMapper;
import com.example.zhizuo.mapper.UserCouponMapper;
import com.example.zhizuo.core.service.OrderService;
import com.example.zhizuo.core.service.OrderSettlementService;
import com.example.zhizuo.api.websocket.OrderWebSocketEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderSettlementService settlementService;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    public IPage<Order> pageWithItems(Page<Order> page, Wrapper<Order> queryWrapper) {
        // 1. 执行主表查询
        IPage<Order> result = this.page(page, queryWrapper);

        // 2. 如果结果为空，直接返回
        if (result.getRecords().isEmpty()) {
            return result;
        }

        // 3. 批量查询关联的商品明细 (避免 N+1 问题)
        // 提取查询到的所有订单 ID
        List<Long> orderIds = result.getRecords().stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        // 一次性查出所有相关商品
        List<OrderItem> allItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().in(OrderItem::getOrderId, orderIds)
        );

        // 4. 在内存中分组：Map<OrderId, List<OrderItem>>
        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 5. 将商品明细填充回对应的订单对象中
        result.getRecords().forEach(order -> {
            order.setProducts(itemMap.getOrDefault(order.getId(), Collections.emptyList()));
        });

        return result;
    }

    @Override
    public Order getDetailWithItems(Long id) {
        // 1. 查询主表
        Order order = this.getById(id);
        if (order == null) {
            return null;
        }

        // 2. 查询子表
        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderId, id)
        );

        // 3. 填充
        order.setProducts(items);

        return order;
    }

    @Override
    public Map<String, Object> getAdminOrderList(String status) {
        // 简单实现，实际项目中可能需要更复杂的逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("count", 0);
        return result;
    }

    @Override
    public void updateOrderStatus(Long id, String status) {
        Order order = this.getById(id);
        if (order != null) {
            order.setStatus(status);
            this.updateById(order);
            
            // 推送 WebSocket 消息（直接调用静态方法，避免循环依赖）
            OrderWebSocketEndpoint.pushOrderStatus(order.getOrderNo(), status);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType, Long userCouponId, String addressInfo, String remark) {
        // 1. 生成订单号
        String orderNo = "ORDER" + IdUtil.getSnowflakeNextIdStr();
        
        // 2. 从数据库查询商品价格，计算订单原价（防止前端篡改）
        BigDecimal originalTotal = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer count = Integer.valueOf(item.get("count").toString());
            
            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在: " + productId);
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new RuntimeException("商品已下架: " + product.getName());
            }
            if (product.getStock() != null && product.getStock() < count) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }
            
            // 使用数据库中的价格，而不是前端传的价格
            BigDecimal itemPrice = product.getPrice().multiply(new BigDecimal(count));
            originalTotal = originalTotal.add(itemPrice);
        }
        
        // 3. 计算优惠后价格（如果使用了优惠券）
        BigDecimal finalAmount = originalTotal;
        BigDecimal discountAmount = BigDecimal.ZERO;
        Long couponId = null;
        
        if (userCouponId != null) {
            try {
                OrderSettlementService.SettlementResult settlement = settlementService.calculateFinalPrice(
                    originalTotal, userCouponId, userId.toString()
                );
                finalAmount = settlement.getFinalPrice();
                discountAmount = settlement.getDiscountAmount();
                couponId = settlement.getUsedCouponId();
                
                // 4. 标记优惠券为已使用
                UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
                if (userCoupon != null) {
                    userCoupon.setStatus(1); // 1:已使用
                    userCoupon.setUseTime(LocalDateTime.now());
                    userCouponMapper.updateById(userCoupon);
                }
            } catch (Exception e) {
                log.error("优惠券结算失败", e);
                throw new RuntimeException("优惠券使用失败: " + e.getMessage());
            }
        }
        
        // 5. 创建订单主表
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setDeliveryType(deliveryType);
        order.setStatus("PENDING");
        order.setPayStatus(0); // 0:未支付
        order.setTotalAmount(finalAmount);
        order.setOriginalAmount(originalTotal);
        order.setDiscountAmount(discountAmount);
        order.setCouponId(couponId);
        order.setAddressInfo(addressInfo);
        order.setCreateTime(LocalDateTime.now());
        this.save(order);
        
        // 6. 创建订单明细
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer count = Integer.valueOf(item.get("count").toString());
            
            Product product = productMapper.selectById(productId);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(count);
            // 注意：OrderItem 实体没有 spec 字段，如果需要规格信息可以添加到实体中
            orderItemMapper.insert(orderItem);
            
            // 7. 扣减库存（可选，根据业务需求）
            // product.setStock(product.getStock() - count);
            // productMapper.updateById(product);
        }
        
        log.info("订单创建成功：订单号={}, 用户ID={}, 原价={}, 优惠={}, 实付={}", 
                orderNo, userId, originalTotal, discountAmount, finalAmount);
        
        return orderNo;
    }

    @Override
    public void closeOrderAndRestoreStock(Long orderId, String reason) {
        Order order = this.getById(orderId);
        if (order != null) {
            order.setStatus("CANCELLED");
            this.updateById(order);
        }
    }

    @Override
    public List<Order> getUserOrderList(Long userId, String status) {
        return this.list(Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return this.getOne(Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderNo, orderNo));
    }
}