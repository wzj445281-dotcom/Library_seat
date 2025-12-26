package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.Arrays;
import java.util.Collections;
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
        IPage<Order> result = this.page(page, queryWrapper);
        if (result.getRecords().isEmpty()) {
            return result;
        }
        fillOrderItems(result.getRecords());
        return result;
    }

    /**
     * ✅ 核心修复：批量填充订单项，并关联商品图片
     */
    private void fillOrderItems(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return;

        // 1. 获取所有订单ID
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        // 2. 查询这些订单的所有明细
        List<OrderItem> allItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().in(OrderItem::getOrderId, orderIds)
        );

        // 3. ✅ 补充：查询商品图片
        if (!allItems.isEmpty()) {
            // 提取所有涉及的商品ID
            List<Long> productIds = allItems.stream()
                    .map(OrderItem::getProductId)
                    .distinct()
                    .collect(Collectors.toList());

            if (!productIds.isEmpty()) {
                // 批量查询商品信息
                List<Product> products = productMapper.selectBatchIds(productIds);
                // 转为 Map<ProductId, ImgUrl>
                Map<Long, String> productImgMap = products.stream()
                        .collect(Collectors.toMap(Product::getId, p -> p.getImgUrl() != null ? p.getImgUrl() : ""));

                // 将图片填入 OrderItem
                for (OrderItem item : allItems) {
                    item.setProductImage(productImgMap.getOrDefault(item.getProductId(), ""));
                }
            }
        }

        // 4. 按订单分组并赋值
        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        orders.forEach(order -> {
            order.setProducts(itemMap.getOrDefault(order.getId(), Collections.emptyList()));
        });
    }

    @Override
    public Order getDetailWithItems(Long id) {
        Order order = this.getById(id);
        if (order == null) return null;

        // 复用 fillOrderItems 逻辑以获取图片
        fillOrderItems(Collections.singletonList(order));

        return order;
    }

    @Override
    public List<Order> getAdminOrderList(String status) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
        if ("PAID".equals(status)) {
            wrapper.eq(Order::getStatus, "PAID");
        } else if ("MAKING".equals(status)) {
            wrapper.eq(Order::getStatus, "MAKING");
        } else if ("READY".equals(status)) {
            wrapper.in(Order::getStatus, Arrays.asList("READY", "WAIT_PICKUP"));
        } else if ("COMPLETED".equals(status)) {
            wrapper.in(Order::getStatus, Arrays.asList("COMPLETED", "CANCELLED"));
        } else if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        List<Order> list = this.list(wrapper);
        fillOrderItems(list);
        return list;
    }

    @Override
    public void updateOrderStatus(Long id, String status) {
        Order order = this.getById(id);
        if (order != null) {
            order.setStatus(status);
            this.updateById(order);
            OrderWebSocketEndpoint.pushOrderStatus(order.getOrderNo(), status);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType, Long userCouponId, String addressInfo, String remark) {
        String orderNo = "ORDER" + IdUtil.getSnowflakeNextIdStr();

        BigDecimal originalTotal = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer count = Integer.valueOf(item.get("count").toString());

            Product product = productMapper.selectById(productId);
            if (product == null) throw new RuntimeException("商品不存在: " + productId);
            if (product.getStatus() != 1) throw new RuntimeException("商品已下架");

            BigDecimal itemPrice = product.getPrice().multiply(new BigDecimal(count));
            originalTotal = originalTotal.add(itemPrice);
        }

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

                UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
                if (userCoupon != null) {
                    userCoupon.setStatus(1);
                    userCoupon.setUseTime(LocalDateTime.now());
                    userCouponMapper.updateById(userCoupon);
                }
            } catch (Exception e) {
                log.error("优惠券结算失败", e);
                // 降级：不使用优惠券
            }
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setDeliveryType(deliveryType);
        order.setStatus("PENDING");
        order.setPayStatus(0);
        order.setTotalAmount(finalAmount);
        order.setOriginalAmount(originalTotal);
        order.setDiscountAmount(discountAmount);
        order.setCouponId(couponId);
        order.setAddressInfo(addressInfo);
        order.setCreateTime(LocalDateTime.now());
        this.save(order);

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
            orderItemMapper.insert(orderItem);
        }

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

    /**
     * ✅ 修复点：C端用户查询，支持状态转换，并调用 fillOrderItems 填充图片
     */
    @Override
    public List<Order> getUserOrderList(Long userId, String status) {
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        if ("current".equals(status)) {
            queryWrapper.in(Order::getStatus, Arrays.asList("PENDING", "PAID", "MAKING", "READY", "WAIT_PICKUP"));
        } else if ("history".equals(status)) {
            queryWrapper.in(Order::getStatus, Arrays.asList("COMPLETED", "CANCELLED", "REFUNDED"));
        } else if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Order::getStatus, status);
        }

        List<Order> list = this.list(queryWrapper);
        // 关键调用：填充图片
        fillOrderItems(list);
        return list;
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return this.getOne(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNo, orderNo));
    }
}