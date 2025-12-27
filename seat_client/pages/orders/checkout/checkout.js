const app = getApp();
const OrderAPI = require('../../../api/order.js');
const UserAPI = require('../../../api/user.js');

Page({
  data: {
    cartList: [],
    totalPrice: 0,
    totalCount: 0,
    address: null,
    remark: '',
    deliveryType: 1, // 1: 配送到座, 2: 自取
    submitting: false,
    userCouponId: null, // 选中的优惠券ID
    couponDiscount: 0,  // 优惠金额
    finalPrice: 0,      // 最终实付
    
    // 优惠券相关
    availableCoupons: [],  // 可用优惠券列表
    selectedCoupon: null   // 选中的优惠券
  },

  onLoad(options) {
    // 1. 获取购物车数据
    const cart = wx.getStorageSync('cart') || [];
    if (cart.length === 0) {
      wx.showToast({ title: '购物车是空的', icon: 'none' });
      setTimeout(() => wx.switchTab({ url: '/pages/menu/index' }), 1500);
      return;
    }
    this.setData({ cartList: cart });
    this.calcTotal();

    // 2. 获取默认地址 (如果有)
    this.loadDefaultAddress();
    
    // 3. 加载可用优惠券
    this.loadAvailableCoupons();
  },

  onShow() {
    // 检查是否有从地址页面返回的选中地址
    const selectedAddress = wx.getStorageSync('selectedAddress');
    if (selectedAddress) {
      this.setData({ address: selectedAddress });
      // 清除缓存，避免下次进入时仍然存在
      wx.removeStorageSync('selectedAddress');
    }
    
    // 检查是否有从优惠券页面返回的选中优惠券
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    
    // 检查是否有从优惠券页面返回的数据
    if (currentPage.data && currentPage.data.selectedCoupon) {
      const selectedCoupon = currentPage.data.selectedCoupon;
      this.setData({
        selectedCoupon: selectedCoupon,
        userCouponId: selectedCoupon.id,
        couponDiscount: selectedCoupon.amount || 0
      });
    }
    
    this.calcTotal();
  },

  /**
   * 加载默认地址
   */
  loadDefaultAddress() {
    // 模拟获取，实际应调用 UserAPI.getDefaultAddress()
    const addr = wx.getStorageSync('defaultAddress');
    if (addr) {
      this.setData({ address: addr });
    }
  },

  /**
   * 加载可用优惠券
   */
  loadAvailableCoupons() {
    // 这里应该调用后端API获取可用优惠券
    // 暂时使用模拟数据
    const mockCoupons = [
      { id: 1, title: '满30减5', amount: 5, minPoint: 30 },
      { id: 2, title: '满50减10', amount: 10, minPoint: 50 },
      { id: 3, title: '新用户专享券', amount: 8, minPoint: 20 }
    ];
    
    // 过滤满足当前订单金额的优惠券
    const currentTotal = parseFloat(this.data.totalPrice) || 0;
    const availableCoupons = mockCoupons.filter(coupon => 
      currentTotal >= coupon.minPoint
    );
    
    this.setData({ availableCoupons });
  },

  /**
   * 选择优惠券
   */
  selectCoupon() {
    if (this.data.availableCoupons.length === 0) {
      wx.showToast({
        title: '暂无可用优惠券',
        icon: 'none'
      });
      return;
    }
    
    // 跳转到优惠券选择页面
    wx.navigateTo({
      url: '/subpackages/coupon/index?select=true&totalPrice=' + this.data.totalPrice
    });
  },

  /**
   * ✅ 增加商品数量
   */
  addItem(e) {
    const index = e.currentTarget.dataset.index;
    const cart = this.data.cartList;

    // 数量 +1
    cart[index].quantity = (cart[index].quantity || 0) + 1;

    this.updateCart(cart);
  },

  /**
   * ✅ 减少商品数量 (减到0则删除)
   */
  reduceItem(e) {
    const index = e.currentTarget.dataset.index;
    const cart = this.data.cartList;

    if (cart[index].quantity > 1) {
      cart[index].quantity--;
      this.updateCart(cart);
    } else {
      // 询问是否删除
      wx.showModal({
        title: '提示',
        content: '确定要移除该商品吗？',
        success: (res) => {
          if (res.confirm) {
            cart.splice(index, 1);
            this.updateCart(cart);
          }
        }
      });
    }
  },

  /**
   * 更新购物车数据 (保存到本地 + 刷新页面 + 重算总价)
   */
  updateCart(cart) {
    this.setData({ cartList: cart });
    wx.setStorageSync('cart', cart); // 同步更新缓存
    this.calcTotal();

    // 如果删光了，自动返回菜单
    if (cart.length === 0) {
      wx.showToast({ title: '购物车已清空', icon: 'none' });
      setTimeout(() => wx.switchTab({ url: '/pages/menu/index' }), 1500);
    }
  },

  /**
   * 计算总价
   */
  calcTotal() {
    const cart = this.data.cartList;
    let total = 0;
    let count = 0;

    cart.forEach(item => {
      total += item.price * item.quantity;
      count += item.quantity;
    });

    // 计算优惠 (这里简单模拟，如果接入了优惠券逻辑需复杂处理)
    const discount = this.data.couponDiscount || 0;
    let final = total - discount;
    if (final < 0) final = 0;

    this.setData({
      totalPrice: total.toFixed(2),
      totalCount: count,
      finalPrice: final.toFixed(2)
    });
  },

  // 选择配送方式
  selectDelivery(e) {
    const type = Number(e.currentTarget.dataset.type);
    this.setData({ deliveryType: type });
  },

  // 选择地址
  chooseAddress() {
    wx.navigateTo({ url: '/pages/address/list?select=true' });
  },

  // 输入备注
  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  // 提交订单
  submitOrder() {
    if (this.data.submitting) return;

    // 校验
    if (this.data.deliveryType === 1 && !this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' });
      return;
    }
    if (this.data.cartList.length === 0) {
      wx.showToast({ title: '购物车为空', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '正在下单...' });

    // 构造后端需要的参数
    const orderData = {
      items: this.data.cartList.map(item => ({
        productId: item.id, // 确保 menu 页面存进去的是 id
        count: item.quantity,
        spec: item.spec || '' // 规格
      })),
      deliveryType: this.data.deliveryType,
      addressInfo: this.data.deliveryType === 1 ? `${this.data.address.contact} ${this.data.address.phone} ${this.data.address.detail}` : '自取',
      remark: this.data.remark,
      userCouponId: this.data.userCouponId
    };

    OrderAPI.createOrder(orderData)
        .then(res => {
          if (res.code === 200) {
            // 下单成功，清空购物车
            wx.removeStorageSync('cart');
            wx.showToast({ title: '下单成功' });

            // 跳转详情或支付
            const orderNo = res.data; // 假设返回订单号
            setTimeout(() => {
              wx.redirectTo({ url: `/pages/orders/detail/detail?orderNo=${orderNo}` });
            }, 1000);
          } else {
            wx.showToast({ title: res.message || '下单失败', icon: 'none' });
          }
        })
        .catch(err => {
          console.error(err);
          wx.showToast({ title: '网络异常', icon: 'none' });
        })
        .finally(() => {
          this.setData({ submitting: false });
          wx.hideLoading();
        });
  }
});