package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Coupon;
import com.example.zhizuo.core.entity.UserCoupon;
import com.example.zhizuo.mapper.CouponMapper;     // 需自行创建
import com.example.zhizuo.mapper.UserCouponMapper; // 需自行创建
import com.example.zhizuo.common.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 优惠券接口 (处理高并发领券)
 */
@RestController
@RequestMapping("/api/app/coupon")
public class CouponController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    // Lua脚本：检查库存 > 0 则扣减，否则返回 -1
    // KEYS[1]: 库存Key
    private static final String DECR_SCRIPT =
            "if (redis.call('get', KEYS[1]) or '0') + 0 > 0 then " +
                    "   return redis.call('decr', KEYS[1]); " +
                    "else " +
                    "   return -1; " +
                    "end";

    /**
     * 运维接口：预热库存到 Redis (重要：上线前或新建券后必须调用)
     * POST /api/app/coupon/init-stock/1
     */
    @PostMapping("/init-stock/{couponId}")
    public ApiResponse<Boolean> initStock(@PathVariable Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            return ApiResponse.error("优惠券不存在");
        }
        // 设置库存 Key，例如 coupon:stock:1 -> 100
        String stockKey = "coupon:stock:" + couponId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(coupon.getStock()));
        return ApiResponse.success(true);
    }

    /**
     * 领券中心：获取所有可领取的优惠券
     */
    @GetMapping("/list")
    public ApiResponse<List<Coupon>> getAvailableCoupons() {
        LambdaQueryWrapper<Coupon> query = new LambdaQueryWrapper<>();
        query.eq(Coupon::getStatus, 1) // 1: 上架状态
                .orderByDesc(Coupon::getAmount);
        return ApiResponse.success(couponMapper.selectList(query));
    }

    /**
     * 领取优惠券 (并发安全版)
     * 1. 检查用户是否已领 (Redis SetNX)
     * 2. 扣减库存 (Redis Lua)
     * 3. 异步/同步 写入数据库
     */
    @PostMapping("/acquire/{couponId}")
    @Transactional(rollbackFor = Exception.class) // 如果数据库写入失败，回滚事务
    public ApiResponse<Boolean> acquireCoupon(@PathVariable Long couponId) {
        String userId = SecurityUtils.getCurrentUserId();

        // Key 定义
        String stockKey = "coupon:stock:" + couponId; // 预热库存Key，需提前将数据库库存写入Redis
        String userLimitKey = "coupon:user:" + couponId + ":" + userId; // 防重Key

        // 1. 防重：每个用户每种券限领 1 张
        // setIfAbsent 相当于 SETNX，返回 true 表示第一次设置(之前没有)
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(userLimitKey, "1");
        if (Boolean.FALSE.equals(isFirst)) {
            return ApiResponse.error("您已经领取过该券啦");
        }

        // 2. 扣减库存：执行 Lua 脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(DECR_SCRIPT, Long.class);
        Long stockResult = redisTemplate.execute(script, Collections.singletonList(stockKey));

        if (stockResult != null && stockResult >= 0) {
            // 3. Redis 扣减成功 -> 写入 MySQL 数据库
            // (生产环境通常发送 MQ 消息异步写入，这里为简化直接写库)

            try {
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setUserId(userId);
                userCoupon.setCouponId(couponId);
                userCoupon.setStatus(0); // 0:未使用
                userCoupon.setCreateTime(LocalDateTime.now());

                userCouponMapper.insert(userCoupon);

                // 同时更新数据库里的库存 (可选，或者定时任务同步)
                // couponMapper.decreaseStock(couponId);

                return ApiResponse.success(true);
            } catch (Exception e) {
                // 如果数据库挂了，需要回滚 Redis 里的状态（非常重要！）
                redisTemplate.delete(userLimitKey); // 允许用户重试
                redisTemplate.opsForValue().increment(stockKey); // 加回库存
                throw e;
            }
        } else {
            // 4. 库存不足
            redisTemplate.delete(userLimitKey); // 既然没领到，清除防重记录，虽然没库存了但也保持逻辑严谨
            return ApiResponse.error("手慢了，优惠券已抢光");
        }
    }

    /**
     * 获取我的优惠券列表
     */
    @GetMapping("/my")
    public ApiResponse<List<UserCoupon>> getMyCoupons() {
        String userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<UserCoupon> query = new LambdaQueryWrapper<>();
        query.eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreateTime);
        // 注意：前端如果需要显示券名和金额，建议根据 couponId 再去匹配 list 接口的数据，
        // 或者让后端在这里进行 VO 转换（需定义 UserCouponVO）
        return ApiResponse.success(userCouponMapper.selectList(query));
    }

    /**
     * 获取可用优惠券列表（用于结算页面）
     * 根据订单金额筛选满足门槛且未使用的优惠券
     * 
     * @param totalPrice 订单总金额
     * @return 可用优惠券列表（包含优惠券详情）
     */
    @GetMapping("/available")
    public ApiResponse<List<Map<String, Object>>> getAvailableCoupons(@RequestParam BigDecimal totalPrice) {
        String userId = SecurityUtils.getCurrentUserId();
        
        // 1. 查询用户所有未使用的优惠券
        LambdaQueryWrapper<UserCoupon> userCouponQuery = new LambdaQueryWrapper<>();
        userCouponQuery.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0) // 0:未使用
                .orderByDesc(UserCoupon::getCreateTime);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(userCouponQuery);
        
        // 2. 查询优惠券模板，筛选满足门槛的
        List<Map<String, Object>> availableList = new java.util.ArrayList<>();
        for (UserCoupon userCoupon : userCoupons) {
            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon != null && coupon.getStatus() == 1) {
                // 检查是否满足使用门槛
                if (totalPrice.compareTo(coupon.getMinPoint()) >= 0) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("userCouponId", userCoupon.getId());
                    item.put("couponId", coupon.getId());
                    item.put("title", coupon.getTitle());
                    item.put("amount", coupon.getAmount());
                    item.put("minPoint", coupon.getMinPoint());
                    item.put("createTime", userCoupon.getCreateTime());
                    availableList.add(item);
                }
            }
        }
        
        // 按优惠金额降序排列（优惠力度大的优先）
        availableList.sort((a, b) -> {
            BigDecimal amountA = (BigDecimal) a.get("amount");
            BigDecimal amountB = (BigDecimal) b.get("amount");
            return amountB.compareTo(amountA);
        });
        
        return ApiResponse.success(availableList);
    }
}