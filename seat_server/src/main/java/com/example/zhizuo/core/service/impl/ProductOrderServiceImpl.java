package com.example.zhizuo.core.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.entity.ProductOrder;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ProductMapper;
import com.example.zhizuo.core.mapper.ProductOrderMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {

    @Resource
    private ProductMapper productMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 创建商品订单
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 购买数量
     * @return 订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, Long productId, Integer quantity) {
        // 1. 校验商品信息
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }

        // 2. 校验库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        // 3. 计算订单金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));

        // 4. 校验用户余额
        User user = userMapper.selectById(userId);
        if (user.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("余额不足");
        }

        // 5. 使用分布式锁扣减库存
        String lockKey = "product_stock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    // 双重检查库存
                    Product currentProduct = productMapper.selectById(productId);
                    if (currentProduct.getStock() < quantity) {
                        throw new RuntimeException("手慢了，库存不足");
                    }

                    // 扣减库存
                    currentProduct.setStock(currentProduct.getStock() - quantity);
                    productMapper.updateById(currentProduct);

                    // 扣减用户余额
                    user.setBalance(user.getBalance().subtract(totalAmount));
                    userMapper.updateById(user);

                    // 6. 创建订单
                    ProductOrder order = new ProductOrder();
                    order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
                    order.setUserId(userId);
                    order.setTotalAmount(totalAmount);
                    order.setStatus("UNPAID"); // 实际已经支付，这里简化处理
                    order.setCreateTime(LocalDateTime.now());
                    order.setPayTime(LocalDateTime.now());

                    this.save(order);

                    log.info("商品订单创建成功 OrderNo: {}, Amount: {}", order.getOrderNo(), totalAmount);
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
}