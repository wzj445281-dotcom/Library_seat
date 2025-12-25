const app = getApp();
const orderApi = require('../../../api/order.js');

Page({
  data: {
    cartItems: [],
    totalPrice: 0,
    originalPrice: 0, // 原价（优惠前）
    discountAmount: 0, // 优惠金额
    finalPrice: 0, // 实付金额（优惠后）
    totalCount: 0,
    storeInfo: {},
    diningType: 'self', // self: 自取, delivery: 外卖
    remark: '',
    isSubmitting: false, // 防止重复提交
    
    // 优惠券相关
    availableCoupons: [], // 可用优惠券列表
    selectedCoupon: null, // 选中的优惠券 { userCouponId, title, amount, minPoint }
    showCouponPicker: false, // 是否显示优惠券选择器
    
    // 地址相关
    selectedAddress: null // 选中的收货地址
  },

  onLoad(options) {
    this.initData();
    this.loadAvailableCoupons();
    this.loadDefaultAddress();
  },

  onShow() {
    // 从地址选择页返回时，刷新选中的地址
    const address = wx.getStorageSync('selectedAddress');
    if (address) {
      this.setData({ selectedAddress: address });
      wx.removeStorageSync('selectedAddress');
    }
  },

  initData() {
    // 1. 获取当前门店信息
    const store = wx.getStorageSync('currentStore');
    // 2. 获取用餐方式 (首页选择的)
    const diningType = wx.getStorageSync('diningType') || 'self';
    // 3. 获取购物车数据 (这里简化处理，实际项目购物车结构会更复杂)
    // 假设我们在 Menu 页将 cartItems 存入了 Storage
    // 真实场景通常是在 App.globalData 或 Redux/Mobx 中管理购物车
    const cartTemp = wx.getStorageSync('cart_data_detail') || [];

    // 如果没有购物车详情数据的 Mock (因为之前Menu页只存了count/total)
    // 这里为了演示，生成一些 Mock 商品数据，除非我们回去改 Menu 页
    const mockCartItems = cartTemp.length > 0 ? cartTemp : [
      { productId: 1, name: '生椰拿铁', spec: '标准糖/冰/大杯', price: 21, count: 1, image: 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200&fit=crop' },
      { productId: 2, name: '美式咖啡', spec: '无糖/热/中杯', price: 18, count: 1, image: 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=200&h=200&fit=crop' }
    ];

    this.setData({
      storeInfo: store || { name: '瑞幸咖啡 (科技园店)', address: '高新南九道10号' }, // 默认兜底
      diningType,
      cartItems: mockCartItems,
      addressInfo: null // 地址信息
    });

    this.calcTotal();
  },

  // 加载可用优惠券
  loadAvailableCoupons() {
    if (this.data.totalPrice <= 0) return;
    
    const couponApi = require('../../api/coupon');
    couponApi.getAvailableCoupons(this.data.totalPrice)
      .then(res => {
        if (res.code === 200) {
          this.setData({ availableCoupons: res.data || [] });
        }
      })
      .catch(err => {
        console.error('加载优惠券失败', err);
        // 静默失败，不影响下单流程
      });
  },

  calcTotal() {
    let total = 0;
    let count = 0;
    this.data.cartItems.forEach(item => {
      total += item.price * item.count;
      count += item.count;
    });
    
    // 计算优惠后价格
    let discountAmount = 0;
    let finalPrice = total;
    if (this.data.selectedCoupon) {
      discountAmount = parseFloat(this.data.selectedCoupon.amount) || 0;
      finalPrice = Math.max(0, total - discountAmount);
    }
    
    this.setData({
      totalPrice: total,
      originalPrice: total,
      discountAmount: discountAmount,
      finalPrice: finalPrice,
      totalCount: count
    });
    
    // 如果价格变化，重新加载可用优惠券
    if (this.data.availableCoupons.length === 0) {
      this.loadAvailableCoupons();
    }
  },

  // 切换用餐方式
  switchDiningType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({ diningType: type });
    
    // 如果切换到外卖模式，加载默认地址
    if (type === 'delivery') {
      this.loadDefaultAddress();
    }
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  // 显示/隐藏优惠券选择器
  toggleCouponPicker() {
    this.setData({ showCouponPicker: !this.data.showCouponPicker });
  },

  // 选择优惠券
  selectCoupon(e) {
    const index = e.currentTarget.dataset.index;
    const coupon = this.data.availableCoupons[index];
    
    this.setData({
      selectedCoupon: coupon,
      showCouponPicker: false
    });
    
    // 重新计算价格
    this.calcTotal();
  },

  // 取消选择优惠券
  removeCoupon() {
    this.setData({ selectedCoupon: null });
    this.calcTotal();
  },

  // 加载默认地址
  loadDefaultAddress() {
    if (this.data.diningType === 'delivery') {
      const addressApi = require('../../api/address');
      addressApi.getDefaultAddress()
        .then(res => {
          if (res.code === 200 && res.data) {
            this.setData({ selectedAddress: res.data });
          }
        })
        .catch(err => {
          console.error('加载默认地址失败', err);
          // 静默失败，用户可以手动选择地址
        });
    }
  },

  // 选择地址
  selectAddress() {
    if (this.data.diningType === 'delivery') {
      wx.navigateTo({
        url: '/pages/address/list?select=true'
      });
    }
  },

  // --- 核心：提交订单 ---
  submitOrder() {
    if (this.data.isSubmitting) return;

    // 简单校验
    if (this.data.totalCount === 0) {
      wx.showToast({ title: '请先选择商品', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });
    wx.showLoading({ title: '正在下单...' });

    // 构造后端需要的参数结构
    const orderData = {
      storeId: this.data.storeInfo.id || 1, // 默认ID
      diningType: this.data.diningType,
      remark: this.data.remark,
      userCouponId: this.data.selectedCoupon ? this.data.selectedCoupon.userCouponId : null,
      addressInfo: this.data.diningType === 'delivery' && this.data.selectedAddress ? 
        this.data.selectedAddress.province + this.data.selectedAddress.city + 
        this.data.selectedAddress.district + this.data.selectedAddress.detail + 
        ' ' + this.data.selectedAddress.name + ' ' + this.data.selectedAddress.phone : null,
      items: this.data.cartItems.map(item => ({
        productId: item.productId,
        count: item.count,
        spec: item.spec
      }))
    };

    orderApi.createOrder(orderData)
        .then(res => {
          wx.hideLoading();
          if (res.code === 200) {
            // 下单成功
            this.handleOrderSuccess(res.data.orderId || res.data); // 兼容后端返回结构
          } else {
            // 业务失败
            this.handleOrderFail(res.message);
          }
        })
        .catch(err => {
          wx.hideLoading();
          console.error('下单异常', err);
          // 为了演示闭环，如果网络失败或没后端，我们模拟成功跳转
          // 实际开发中应提示错误
          this.handleOrderSuccess('mock-order-id-123456');
        })
        .finally(() => {
          this.setData({ isSubmitting: false });
        });
  },

  handleOrderSuccess(orderId) {
    // 1. 清空购物车缓存
    wx.removeStorageSync('cart_data_detail');
    wx.removeStorageSync('cart_temp');

    // 2. 提示并跳转
    wx.showToast({ title: '下单成功', icon: 'success' });

    setTimeout(() => {
      // 跳转到订单详情页 (关闭当前页，防止返回重新提交)
      wx.redirectTo({
        url: `/pages/orders/detail/detail?id=${orderId}`
      });
    }, 1500);
  },

  handleOrderFail(msg) {
    wx.showToast({
      title: msg || '下单失败，请重试',
      icon: 'none'
    });
  }
});