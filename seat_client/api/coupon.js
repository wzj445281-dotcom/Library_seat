const request = require('../utils/request.js');

module.exports = {
  /**
   * 获取优惠券列表
   */
  getCouponList() {
    return request.get('/api/app/coupon/list');
  },

  /**
   * 获取我的优惠券
   */
  getMyCoupons() {
    return request.get('/api/app/coupon/my');
  },

  /**
   * 领取优惠券
   * @param {number} couponId 优惠券ID
   */
  receiveCoupon(couponId) {
    return request.post(`/api/app/coupon/acquire/${couponId}`);
  },

  /**
   * 获取可用优惠券列表（用于结算页面）
   * @param {number} totalPrice 订单总金额
   */
  getAvailableCoupons(totalPrice) {
    return request.get('/api/app/coupon/available', { totalPrice });
  }
};

