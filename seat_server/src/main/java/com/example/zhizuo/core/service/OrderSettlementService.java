package com.example.zhizuo.core.service;

import com.example.zhizuo.entity.Coupon;
import com.example.zhizuo.entity.UserCoupon;
import com.example.zhizuo.mapper.CouponMapper;
import com.example.zhizuo.mapper.UserCouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单结算与算价服务
 */
@Service
public class OrderSettlementService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    /**
     * 计算订单最终金额
     * @param originalTotal 原价总金额 (从数据库商品表计算得出，严禁相信前端传值)
     * @param userCouponId 用户选择的优惠券记录ID (可为 null)
     * @param userId 当前用户ID
     * @return 结算结果 VO (包含：应付金额、优惠金额、实付金额)
     */
    public SettlementResult calculateFinalPrice(BigDecimal originalTotal, Long userCouponId, String userId) {
        SettlementResult result = new SettlementResult();
        result.setOriginalPrice(originalTotal);
        result.setFinalPrice(originalTotal);
        result.setDiscountAmount(BigDecimal.ZERO);

        // 如果没有选择优惠券，直接返回原价
        if (userCouponId == null) {
            return result;
        }

        // 1. 校验优惠券归属和状态
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new RuntimeException("非法操作：优惠券不存在或不属于该用户");
        }
        if (userCoupon.getStatus() != 0) { // 0:未使用
            throw new RuntimeException("优惠券已使用或已过期");
        }

        // 2. 获取优惠券模板详情 (查金额和门槛)
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券模板已失效");
        }

        // 3. 校验门槛 (minPoint)
        // compareTo: -1 (小于), 0 (等于), 1 (大于)
        if (originalTotal.compareTo(coupon.getMinPoint()) < 0) {
            throw new RuntimeException("未满足优惠券使用门槛，满 " + coupon.getMinPoint() + " 元可用");
        }

        // 4. 计算优惠后金额
        BigDecimal discount = coupon.getAmount();
        BigDecimal finalPrice = originalTotal.subtract(discount);

        // 防止金额变为负数
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO; // 或者 0.01，视业务规定而定
        }

        result.setDiscountAmount(discount);
        result.setFinalPrice(finalPrice);
        result.setUsedCouponId(userCouponId); // 标记使用了哪张券

        return result;
    }

    /**
     * 简单的结算结果内部类
     */
    public static class SettlementResult {
        private BigDecimal originalPrice; // 原价
        private BigDecimal discountAmount; // 优惠金额
        private BigDecimal finalPrice; // 实付
        private Long usedCouponId; // 使用的券ID

        // Getters & Setters ...
        public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
        public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
        public void setUsedCouponId(Long usedCouponId) { this.usedCouponId = usedCouponId; }
    }
}