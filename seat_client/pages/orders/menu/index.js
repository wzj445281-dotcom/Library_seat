const request = require('../../utils/request.js');

Page({
  data: {
    categories: [
      { id: 1, name: '人气热销' },
      { id: 2, name: '大师咖啡' },
      { id: 3, name: '生椰家族' },
      { id: 4, name: '瑞纳冰' }
    ],
    currentCategory: 1,
    products: [],

    // 规格弹窗相关数据
    showSpecModal: false,
    currentProduct: null,
    currentSpecs: {
      temp: '冰',
      sugar: '标准糖'
    },

    // 购物车数据
    cart: [],
    totalCount: 0,
    totalPrice: '0.00'
  },

  onLoad() {
    this.loadProducts(1);
    // 从缓存恢复购物车
    const cachedCart = wx.getStorageSync('cart_items') || [];
    this.setData({ cart: cachedCart });
    this.calcTotal();
  },

  switchCategory(e) {
    const id = e.currentTarget.dataset.id;
    this.setData({ currentCategory: id });
    this.loadProducts(id);
  },

  loadProducts(categoryId) {
    // 模拟数据：实际应请求后端
    // 为了演示效果，这里稍微丰富一下商品描述
    const mockProducts = [
      { id: 101, name: '生椰拿铁', price: '18.00', desc: 'YYDS，甚至不需要介绍', image: '/assets/images/book-default.png' },
      { id: 102, name: '厚乳拿铁', price: '16.00', desc: '乳蛋白含量提升，口感更醇厚', image: '/assets/images/book-default.png' },
      { id: 103, name: '标准美式', price: '13.00', desc: 'IIAC金奖豆，经典之选', image: '/assets/images/book-default.png' },
      { id: 104, name: '橙C美式', price: '16.00', desc: 'NFC鲜榨橙汁+浓缩咖啡', image: '/assets/images/book-default.png' }
    ];
    this.setData({ products: mockProducts });
  },

  // --- 规格弹窗逻辑 ---

  // 打开弹窗
  openSpecModal(e) {
    const product = e.currentTarget.dataset.item;
    this.setData({
      showSpecModal: true,
      currentProduct: product,
      // 重置为默认规格
      currentSpecs: { temp: '冰', sugar: '标准糖' }
    });
  },

  // 关闭弹窗
  closeSpecModal() {
    this.setData({ showSpecModal: false });
  },

  // 选择规格
  selectSpec(e) {
    const { key, val } = e.currentTarget.dataset;
    // 使用 setData 的动态 key 更新局部数据
    this.setData({
      [`currentSpecs.${key}`]: val
    });
  },

  // 从弹窗加入购物车
  addToCartFromModal() {
    const product = this.data.currentProduct;
    const specs = this.data.currentSpecs;
    const specStr = `${specs.temp}/${specs.sugar}`; // 生成规格字符串，如 "冰/半糖"

    // 生成唯一key，用于区分同商品不同规格
    // 格式：商品ID_温度_糖度
    const uniqueKey = `${product.id}_${specs.temp}_${specs.sugar}`;

    let cart = this.data.cart;
    // 查找购物车中是否已有该商品+规格
    const existingItemIndex = cart.findIndex(item => item.uniqueKey === uniqueKey);

    if (existingItemIndex !== -1) {
      // 已存在，数量+1
      cart[existingItemIndex].count += 1;
    } else {
      // 不存在，新增条目
      cart.push({
        id: product.id,
        uniqueKey: uniqueKey, // 关键：区分不同规格
        name: product.name,
        price: product.price,
        image: product.image,
        specs: specStr, // 用于展示
        count: 1
      });
    }

    this.setData({ cart, showSpecModal: false });
    this.calcTotal();

    wx.showToast({ title: '已加入购物车', icon: 'success', duration: 1000 });
  },

  // --- 购物车基础逻辑 ---

  // 查看购物车详情（可选功能，暂未实现弹窗，可只用作 console.log 或保留空函数）
  showCartDetail() {
    // 如果需要实现点击购物车图标弹出明细，可以在这里添加逻辑
    // 目前主要依赖跳转结算页查看明细
    if (this.data.totalCount > 0) {
      // wx.navigateTo({ url: '/pages/orders/checkout/checkout' }); // 可选：直接跳转
    }
  },

  calcTotal() {
    const cart = this.data.cart;
    let count = 0;
    let price = 0;
    cart.forEach(item => {
      count += item.count;
      price += item.count * parseFloat(item.price);
    });
    this.setData({
      totalCount: count,
      totalPrice: price.toFixed(2)
    });
    // 实时存入缓存
    wx.setStorageSync('cart_items', cart);
  },

  goToCheckout() {
    if (this.data.totalCount === 0) {
      wx.showToast({ title: '请先选购商品', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: '/pages/orders/checkout/checkout' });
  }
});