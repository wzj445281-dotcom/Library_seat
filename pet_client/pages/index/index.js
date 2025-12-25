const app = getApp(); 
const request = require('../../utils/request.js'); 
 
Page({ 
  data: { 
    banners: [ 
      '/assets/images/seat.png', // 暂时用现有图片占位，建议换成宠物Banner 
      '/assets/images/seat-active.png' 
    ], 
    categories: [ 
      { id: 'food', name: '主粮' }, 
      { id: 'snack', name: '零食' }, 
      { id: 'toy', name: '玩具' }, 
      { id: 'care', name: '洗护' } 
    ], 
    currentCategory: 'food', 
    productList: [], // 商品列表 
    loading: false 
  }, 
 
  onLoad: function () { 
    this.loadProducts(); 
  }, 
 
  // 1. 加载商品列表 
  loadProducts() { 
    this.setData({ loading: true }); 
     
    // 调用我们在后端 AppShopController 写的接口 
    // request.get 封装了 wx.request 
    const url = `/app/shop/products?category=${this.data.currentCategory}`; 
     
    // 模拟数据（如果后端还没跑起来，先用这个看效果） 
    const mockData = [ 
      { id: 1, name: '全价猫粮 10kg', price: 199.00, image: '/assets/images/seat.png', sales: 100 }, 
      { id: 2, name: '宠物自动饮水机', price: 89.00, image: '/assets/images/seat-active.png', sales: 50 }, 
      { id: 3, name: '磨牙棒', price: 9.90, image: '/assets/images/user.png', sales: 200 }, 
    ]; 
 
    // 实际请求 (取消注释使用) 
    /* 
    request.get(url).then(res => { 
      this.setData({ productList: res.data, loading: false }); 
    }).catch(err => { 
      // 降级使用模拟数据 
      this.setData({ productList: mockData, loading: false }); 
    }); 
    */ 
     
    // 暂时直接设置模拟数据 
    this.setData({ productList: mockData, loading: false }); 
  }, 
 
  // 2. 切换分类 
  switchCategory(e) { 
    const category = e.currentTarget.dataset.id; 
    this.setData({ currentCategory: category }, () => { 
      this.loadProducts(); 
    }); 
  }, 
 
  // 3. 点击购买（下单） 
  handleBuy(e) { 
    const product = e.currentTarget.dataset.item; 
     
    wx.showModal({ 
      title: '确认购买', 
      content: `确定要支付 ¥${product.price} 购买 ${product.name} 吗？`, 
      success: (res) => { 
        if (res.confirm) { 
          this.createOrder(product.id); 
        } 
      } 
    }); 
  }, 
 
  // 4. 发送下单请求 
  createOrder(productId) { 
    wx.showLoading({ title: '创建订单中...' }); 
     
    request.post('/app/shop/order/create', { 
      productId: productId, 
      count: 1 // 默认买1个 
    }).then(res => { 
      wx.hideLoading(); 
      if (res.code === 200) { 
        wx.showToast({ title: '下单成功', icon: 'success' }); 
        // 可以跳转到订单详情页 
        // wx.navigateTo({ url: '/pages/orders/detail?no=' + res.data }); 
      } else { 
        wx.showToast({ title: res.msg || '库存不足', icon: 'none' }); 
      } 
    }).catch(() => { 
      wx.hideLoading(); 
      // 模拟成功 
      wx.showToast({ title: '模拟下单成功', icon: 'success' }); 
    }); 
  }, 
 
  // 跳转详情页 
  goToDetail(e) { 
    const id = e.currentTarget.dataset.id; 
    wx.navigateTo({ 
      url: `/pages/shop/detail?id=${id}` // 假设你有这个页面，没有的话可以先不做 
    }); 
  } 
});