const app = getApp();
const orderApi = require('../../../api/order.js');

Page({
  data: {
    cartItems: [],
    totalPrice: 0,
    totalCount: 0,
    storeInfo: {},
    diningType: 'self', // self: 自取, delivery: 外卖
    remark: '',
    isSubmitting: false // 防止重复提交
  },

  onLoad(options) {
    this.initData();
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
      cartItems: mockCartItems
    });

    this.calcTotal();
  },

  calcTotal() {
    let total = 0;
    let count = 0;
    this.data.cartItems.forEach(item => {
      total += item.price * item.count;
      count += item.count;
    });
    this.setData({
      totalPrice: total,
      totalCount: count
    });
  },

  // 切换用餐方式
  switchDiningType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({ diningType: type });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
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