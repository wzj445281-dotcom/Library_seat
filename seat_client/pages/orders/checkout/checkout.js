const request = require('../../utils/request.js');

Page({
  data: {
    orderType: 1, // 1:自取, 2:外卖
    cartItems: [],
    itemTotal: '0.00', // 商品原价
    totalCount: 0,
    totalPrice: '0.00', // 最终支付价
    discountAmount: '0.00',
    address: '',
    phone: '',

    // 优惠券数据
    coupons: [
      { id: 101, name: '☕️ 新人首单立减', amount: 5 },
      { id: 102, name: '📅 周一咖啡日满减', amount: 3 },
      { id: 103, name: '🎁 会员专属福利', amount: 2 }
    ],
    selectedCoupon: null
  },

  onLoad(options) {
    if (options.type) {
      this.setData({ orderType: parseInt(options.type) });
    }
    const items = wx.getStorageSync('cart_items') || [];
    this.setData({ cartItems: items });

    this.getUserAddress();
    this.calcTotal(); // 初始计算
  },

  getUserAddress() {
    // 模拟获取地址，实际应调用 request
    this.setData({
      address: '科技园南区 R2-A 301',
      phone: '13800138000'
    });
  },

  // 计算总价逻辑
  calcTotal() {
    let count = 0;
    let price = 0;
    this.data.cartItems.forEach(item => {
      count += item.count;
      price += item.count * parseFloat(item.price);
    });

    // 计算优惠
    const discount = this.data.selectedCoupon ? this.data.selectedCoupon.amount : 0;
    let finalPrice = price - discount;
    if (finalPrice < 0) finalPrice = 0;

    this.setData({
      totalCount: count,
      itemTotal: price.toFixed(2),
      discountAmount: discount.toFixed(2),
      totalPrice: finalPrice.toFixed(2)
    });
  },

  // 切换配送方式
  switchType(e) {
    this.setData({ orderType: parseInt(e.currentTarget.dataset.type) });
  },

  // 输入监听
  onAddressInput(e) { this.setData({ address: e.detail.value }); },
  onPhoneInput(e) { this.setData({ phone: e.detail.value }); },

  // 选择优惠券
  onCouponChange(e) {
    const index = e.detail.value;
    const coupon = this.data.coupons[index];
    this.setData({ selectedCoupon: coupon });
    this.calcTotal(); // 重新计算价格
    wx.showToast({ title: '已应用优惠', icon: 'none' });
  },

  // 提交订单
  submitOrder() {
    // 校验
    if (this.data.orderType === 2 && (!this.data.address || !this.data.phone)) {
      wx.showToast({ title: '请完善配送信息', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '正在创建订单...' });

    // 构造请求参数
    const payload = {
      deliveryType: this.data.orderType - 1,
      items: this.data.cartItems.map(i => ({
        productId: i.id,
        quantity: i.quantity || i.count
      })),
      addressInfo: this.data.address,
      phone: this.data.phone,
      couponId: this.data.selectedCoupon ? this.data.selectedCoupon.id : null,
      actualPrice: this.data.totalPrice
    };

    // 模拟网络请求
    setTimeout(() => {
      // 假设创建成功，得到订单号
      const mockOrderNo = "OD" + new Date().getTime();
      this.simulatePay(mockOrderNo);
    }, 800);
  },

  // 模拟支付过程 (核心任务 C)
  simulatePay(orderNo) {
    wx.hideLoading();
    wx.showLoading({ title: '支付中...', mask: true });

    setTimeout(() => {
      wx.hideLoading();

      // 模拟支付成功弹窗
      wx.showToast({ title: '支付成功', icon: 'success', duration: 2000 });

      // 清空购物车
      wx.removeStorageSync('cart_items');

      // 延迟跳转
      setTimeout(() => {
        // 跳转到订单列表，或者详情页
        wx.redirectTo({ url: '/pages/orders/list' });
      }, 1500);
    }, 1500);
  }
});