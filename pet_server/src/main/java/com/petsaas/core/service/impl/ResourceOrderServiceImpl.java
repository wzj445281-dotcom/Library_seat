package com.petsaas.core.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.common.ApiResponse;
import com.petsaas.core.entity.Product;
import com.petsaas.core.entity.ProductOrder;
import com.petsaas.core.mapper.ProductMapper;
import com.petsaas.core.mapper.ProductOrderMapper;
import com.petsaas.core.service.ProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 物资流转核心服务
 * 实现库存扣减、配送校验、费用计算的状态机流转
 */
@Slf4j
@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {

    @Resource
    private ProductMapper productMapper;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建物资借用订单
     * @param userId 用户ID
     * @param resourceId 物资ID
     * @param deliveryType 0=自取, 1=配送到�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, Long resourceId, Integer deliveryType) {
        // 1. 校验物资基础信息
        Resources resource = resourcesMapper.selectById(resourceId);
        if (resource == null || resource.getStatus() == 0) {
            throw new RuntimeException("物资不存在或已下�?);
        }

        Long seatId = null;
        // 2. 配送模式校验：必须有正在使用中的座�?
        if (deliveryType == 1) {
            Reservation activeRes = getActiveReservation(userId);
            if (activeRes == null) {
                throw new RuntimeException("您当前不在座位上，无法使用配送服�?);
            }
            seatId = activeRes.getSeatId();
        }

        // 3. Redisson 分布式锁扣减库存 (防止超卖)
        String lockKey = "resource_stock:" + resourceId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试加锁 5秒等待，10秒自动释�?
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    // 双重检查库�?
                    Resources currentRes = resourcesMapper.selectById(resourceId);
                    if (currentRes.getStock() <= 0) {
                        throw new RuntimeException("手慢了，库存不足");
                    }

                    // 扣减库存
                    currentRes.setStock(currentRes.getStock() - 1);
                    resourcesMapper.updateById(currentRes);

                    // 4. 创建订单
                    ResourceOrder order = new ResourceOrder();
                    order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
                    order.setUserId(userId);
                    order.setResourceId(resourceId);
                    order.setSeatId(seatId);
                    order.setDeliveryType(deliveryType);
                    // 如果是自取，状态为待取货；如果是配送，状态为待配�?
                    order.setStatus(deliveryType == 0 ? "PICKING" : "DELIVERING");
                    order.setCreateTime(LocalDateTime.now());

                    this.save(order);

                    log.info("物资订单创建成功 OrderNo: {}", order.getOrderNo());
                    return order.getOrderNo();

                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("系统异常");
        }
    }

    /**
     * 归还物资并结�?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnResource(String orderNo) {
        // 1. 查询订单
        QueryWrapper<ResourceOrder> query = new QueryWrapper<>();
        query.eq("order_no", orderNo);
        ResourceOrder order = this.getOne(query);

        if (order == null || "RETURNED".equals(order.getStatus())) {
            throw new RuntimeException("订单不存在或已归�?);
        }

        // 2. 更新状�?
        order.setStatus("RETURNED");
        order.setReturnTime(LocalDateTime.now());

        // 3. 计算费用 (简单策略：按小时计�?
        Resources resource = resourcesMapper.selectById(order.getResourceId());
        if (resource.getHourlyCost() > 0 && order.getStartUseTime() != null) {
            long hours = Duration.between(order.getStartUseTime(), order.getReturnTime()).toHours();
            // 不足1小时�?小时�?
            hours = hours == 0 ? 1 : hours;
            int cost = (int) (hours * resource.getHourlyCost());
            order.setTotalCost(cost);

            // TODO: 调用钱包服务扣费 walletService.deduct(order.getUserId(), cost);
        }

        this.updateById(order);

        // 4. 归还库存 (加锁更安全，虽然这里主要�?1)
        Resources currentRes = resourcesMapper.selectById(order.getResourceId());
        currentRes.setStock(currentRes.getStock() + 1);
        resourcesMapper.updateById(currentRes);
    }

    private Reservation getActiveReservation(Long userId) {
        QueryWrapper<Reservation> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .eq("status", "CHECKED_IN"); // 必须是已签到状�?
        return reservationMapper.selectOne(query);
    }
}