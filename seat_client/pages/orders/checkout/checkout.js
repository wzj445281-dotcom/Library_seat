const request = require('../../utils/request.js');

Page({
  data: {
    orderType: 1, // 1:自取, 2:外卖
    cartItems: [],
    totalCount: 0,
    totalPrice: '0.00',
    address: '',
    phone: ''
  },

  onLoad(options) {
    // 1. 获取 URL 传递的 type
    if (options.type) {
      this.setData({ orderType: parseInt(options.type) });
    }

    // 2. 从缓存获取购物车数据
    const items = wx.getStorageSync('cart_items') || [];
    this.setData({ cartItems: items });
    this.calcTotal(items);
    
    // 3. 获取用户地址信息
    this.getUserAddress();
  },

  // 获取用户地址信息
  getUserAddress() {
    request.get('/api/app/user/address').then(res => {
      if (res.code === 200 && res.data) {
        this.setData({
          address: res.data.address || '',
          phone: res.data.phone || ''
        });
      }
    }).catch(err => {
      console.error('获取地址信息失败:', err);
    });
  },

  calcTotal(items) {
    let count = 0;
    let price = 0;
    items.forEach(item => {
      count += item.count;
      price += item.count * item.price;
    });
    this.setData({
      totalCount: count,
      totalPrice: price.toFixed(2)
    });
  },

  switchType(e) {
    this.setData({ orderType: parseInt(e.currentTarget.dataset.type) });
  },

  onAddressInput(e) {
    this.setData({ address: e.detail.value });
  },
  
  onPhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  // 提交订单
  submitOrder() {
    // 校验
    if (this.data.orderType === 2 && !this.data.address) {
      wx.showToast({ title: '请填写配送地址', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '正在创建订单...' });

    // 构造请求参数
    const payload = {
      deliveryType: this.data.orderType - 1, // 后端0=自取，1=外卖，前端1=自取，2=外卖
      items: this.data.cartItems.map(i => ({ 
        productId: i.id, 
        quantity: i.quantity || i.count 
      })),
      addressInfo: this.data.address,
      phone: this.data.phone
    };

    // 1. 创建订单
    request.post('/api/app/store/order/create', payload).then(res => {
      wx.hideLoading();
      if (res.code === 200) {
        const orderNo = res.data; // 返回订单号
        
        // 2. 模拟支付 (实际项目中这里会调用 wx.requestPayment)
        this.simulatePay(orderNo);
      } else {
        wx.showToast({ title: res.message || '下单失败', icon: 'none' });
      }
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: '网络异常', icon: 'none' });
      console.error(err);
    });
  },

  // 模拟支付过程
  simulatePay(orderNo) {
    wx.showLoading({ title: '正在支付...' });
    
    setTimeout(() => {
      request.post('/api/app/store/order/pay', { orderNo: orderNo }).then(res => {
        wx.hideLoading();
        if (res.code === 200) {
          wx.showToast({ title: '支付成功', icon: 'success' });
          // 清空购物车缓存
          wx.removeStorageSync('cart_items');
          
          // 延迟跳转到订单列表或详情
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/orders/list' }); // 使用redirectTo而不是switchTab
          }, 1500);
        } else {
          wx.showToast({ title: '支付失败', icon: 'none' });
        }
      }).catch(err => {
        wx.hideLoading();
        wx.showToast({ title: '支付异常', icon: 'none' });
        console.error('支付错误:', err);
      });
    }, 1000); // 模拟网络延迟
  }
});