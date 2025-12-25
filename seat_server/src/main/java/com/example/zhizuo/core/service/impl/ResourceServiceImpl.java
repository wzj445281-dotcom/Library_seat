// ... (保留原有的 imports)
package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.ResourceOrder;
import com.example.zhizuo.core.entity.Resources;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ReservationMapper;
import com.example.zhizuo.core.mapper.ResourceOrderMapper;
import com.example.zhizuo.core.mapper.ResourcesMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ResourceServiceImpl extends ServiceImpl<ResourcesMapper, Resources> implements ResourceService {

    @Resource
    private ResourceOrderMapper orderMapper;
    @Resource
    private ReservationMapper reservationMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String CART_KEY_PREFIX = "seat:cart:";

    // ... (batchImport 等原有代码保持不变，此处省略以节省篇幅，请保留原有的 batchImport 实现) ...
    // 为了确保代码完整性，请保留之前生成的 batchImport, addToCart, removeFromCart, getCartList, submitCart 等方法
    // 下面只展示新增/修改的核心方法

    @Override
    public List<ResourceOrder> getOrders(String status) {
        QueryWrapper<ResourceOrder> query = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");
        return orderMapper.selectList(query);
    }

    /**
     * 核心：处理订单状态流转
     * action: DELIVER (发货/开始使用), RETURN (归还)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Long orderId, String action) {
        ResourceOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        if ("DELIVER".equals(action)) {
            // 发货/确认借出
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException("当前状态不可发货");
            }
            order.setStatus("USING"); // 这里简化状态，统一为使用中（含配送中）
            orderMapper.updateById(order);

        } else if ("RETURN".equals(action)) {
            // 归还
            if ("RETURNED".equals(order.getStatus())) {
                throw new RuntimeException("该订单已归还");
            }

            // 1. 更新订单状态
            order.setStatus("RETURNED");
            order.setReturnTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 2. 恢复库存 (关键!)
            boolean updateStock = this.update().setSql("stock = stock + 1")
                    .eq("id", order.getResourceId()).update();
            if (!updateStock) {
                throw new RuntimeException("库存恢复失败");
            }

            log.info("订单 {} 已归还，库存已恢复", orderId);
        } else {
            throw new RuntimeException("未知操作");
        }
    }

    // ... (保留之前的 borrow, addToCart, submitCart, createOrder 等所有方法) ...
    // 请确保之前 ResourceServiceImpl.java 中的所有逻辑都被保留

    // ------ 以下是补全之前的依赖方法，防止覆盖丢失 ------
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchImport(MultipartFile file) {
        // ... (保持之前的实现)
        return 0; // 占位，请保留原代码
    }

    @Override
    public String borrow(Long userId, Long resourceId, Integer deliveryType) {
        // ... (保持之前的实现)
        return null;
    }

    @Override
    public void addToCart(Long userId, Long resourceId) {
        stringRedisTemplate.opsForSet().add(CART_KEY_PREFIX + userId, resourceId.toString());
    }

    @Override
    public void removeFromCart(Long userId, Long resourceId) {
        stringRedisTemplate.opsForSet().remove(CART_KEY_PREFIX + userId, resourceId.toString());
    }

    @Override
    public List<Resources> getCartList(Long userId) {
        Set<String> resourceIds = stringRedisTemplate.opsForSet().members(CART_KEY_PREFIX + userId);
        if (resourceIds == null || resourceIds.isEmpty()) return Collections.emptyList();
        return this.listByIds(resourceIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> submitCart(Long userId, Integer deliveryType) {
        // ... (保持之前的实现，此处省略)
        return new ArrayList<>();
    }
}