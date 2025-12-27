// seat_client/pages/menu/index.js
const app = getApp()

Page({
  data: {
    loading: true,
    activeCategory: 0,
    scrollIntoView: '',
    searchKeyword: '',
    showSearchHistory: false,
    searchHistory: [], // 搜索历史

    // 左侧分类导航
    categories: ['热门销量', '人气推荐', '大师咖啡', '瑞纳冰', '经典烘焙'],

    // 右侧商品列表数据
    products: [
      {
        id: 'cat_hot',
        name: '热门销量',
        items: [
          { id: 1, name: '生椰拿铁', desc: 'YYDS！3年销3亿杯，生椰乳与浓缩咖啡的完美融合。', price: '18', image: '/assets/images/coconut_latte.jpg' },
          { id: 2, name: '加浓美式', desc: '提神醒脑，加倍浓缩，口感醇厚。', price: '13', image: '/assets/images/american.jpg' },
          { id: 3, name: '丝绒拿铁', desc: '北海道丝绒风味，口感如天鹅绒般顺滑。', price: '19', image: '/assets/images/latte.jpg' }
        ]
      },
      {
        id: 'cat_recommend',
        name: '人气推荐',
        items: [
          { id: 4, name: '椰云拿铁', desc: '口感绵密如云朵，清甜椰香，一口惊艳。', price: '19', image: '/assets/images/coconut_cloud.jpg' },
          { id: 5, name: '厚乳拿铁', desc: '精选冷萃厚牛乳，醇厚浓郁，奶香四溢。', price: '18', image: '/assets/images/latte.jpg' }
        ]
      },
      {
        id: 'cat_coffee',
        name: '大师咖啡',
        items: [
          { id: 6, name: '标准美式', desc: 'IIAC金奖豆，经典风味，回甘明显。', price: '13', image: '/assets/images/american.jpg' },
          { id: 7, name: '拿铁', desc: '经典奶咖，浓缩咖啡与牛奶的黄金比例。', price: '16', image: '/assets/images/latte.jpg' },
          { id: 8, name: '卡布奇诺', desc: '奶泡丰富细腻，口感层次丰富。', price: '16', image: '/assets/images/cappuccino.jpg' },
          { id: 9, name: '焦糖玛奇朵', desc: '香草风味糖浆与焦糖酱的甜蜜交织。', price: '17', image: '/assets/images/latte.jpg' }
        ]
      },
      {
        id: 'cat_ice',
        name: '瑞纳冰',
        items: [
          { id: 10, name: '巧克力瑞纳冰', desc: '浓郁巧克力风味，冰爽口感，夏日必备。', price: '21', image: '/assets/images/choco_ice.jpg' },
          { id: 11, name: '抹茶瑞纳冰', desc: '清新抹茶风味，口感细腻，茶香浓郁。', price: '21', image: '/assets/images/matcha_ice.jpg' }
        ]
      },
      {
        id: 'cat_bakery',
        name: '经典烘焙',
        items: [
          { id: 12, name: '提拉米苏风味大福', desc: 'Q弹软糯，内馅丰富，一口满足。', price: '9', image: '/assets/images/logo.png' },
          { id: 13, name: '半熟芝士', desc: '入口即化，芝士香气浓郁，甜而不腻。', price: '12', image: '/assets/images/logo.png' }
        ]
      }
    ],

    // 购物车与规格相关状态
    cartCount: 0,
    totalPrice: 0,
    cartScale: '', // 购物车动画类名
    favoriteMap: {}, // 收藏状态映射

    // 规格弹窗数据
    showSpecModal: false,
    specProduct: null,
    specSelections: {
      temp: '冰',
      sugar: '标准糖'
    },

    // 抛物线小球
    balls: [],
  },

  onLoad() {
    this.initBalls();
    this.loadSearchHistory();
    this.loadCartData(); // 加载购物车数据

    // 模拟网络请求延迟，展示骨架屏
    setTimeout(() => {
      this.setData({ loading: false });
    }, 1000);
  },

  // 加载购物车数据
  loadCartData() {
    const cart = wx.getStorageSync('cart') || [];
    let totalCount = 0;
    let totalPrice = 0;
    
    // 计算购物车商品总数和总价
    cart.forEach(item => {
      totalCount += item.quantity || 0;
      totalPrice += (item.price || 0) * (item.quantity || 0);
    });
    
    this.setData({
      cartCount: totalCount,
      totalPrice: totalPrice
    });
  },

  // --- 搜索相关 ---
  loadSearchHistory() {
    const history = wx.getStorageSync('searchHistory') || [];
    this.setData({ searchHistory: history });
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  onSearchFocus() {
    this.setData({ showSearchHistory: true });
  },

  onSearchBlur() {
    // 延迟关闭，以便点击历史标签
    setTimeout(() => {
      this.setData({ showSearchHistory: false });
    }, 200);
  },

  clearSearch() {
    this.setData({ searchKeyword: '', showSearchHistory: true });
  },

  onSearchConfirm() {
    const keyword = this.data.searchKeyword;
    if (!keyword) return;

    this.saveHistory(keyword);
    this.doSearch(keyword);
  },

  saveHistory(keyword) {
    let history = this.data.searchHistory;
    // 去重并添加到头部
    history = history.filter(item => item !== keyword);
    history.unshift(keyword);
    // 限制历史记录数量
    if (history.length > 10) history.pop();

    this.setData({ searchHistory: history });
    wx.setStorageSync('searchHistory', history);
  },

  clearSearchHistory() {
    this.setData({ searchHistory: [] });
    wx.removeStorageSync('searchHistory');
  },

  selectHistory(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ searchKeyword: keyword });
    this.doSearch(keyword);
  },

  doSearch(keyword) {
    wx.showToast({ title: `搜索: ${keyword}`, icon: 'none' });
    // 这里添加实际搜索逻辑
  },

  // --- 分类切换 ---
  switchCategory(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      activeCategory: index,
      scrollIntoView: this.data.products[index].id
    });
  },

  // --- 收藏功能 ---
  onToggleFavorite(e) {
    const id = e.currentTarget.dataset.id;
    const favoriteMap = this.data.favoriteMap;
    favoriteMap[id] = !favoriteMap[id];

    this.setData({ favoriteMap });

    wx.showToast({
      title: favoriteMap[id] ? '已收藏' : '取消收藏',
      icon: 'none'
    });
  },

  // --- 规格弹窗逻辑 ---
  openSpecModal(e) {
    const product = e.currentTarget.dataset.item;
    this.setData({
      showSpecModal: true,
      specProduct: product,
      // 重置选项
      specSelections: { temp: '冰', sugar: '标准糖' }
    });
  },

  closeSpecModal() {
    this.setData({ showSpecModal: false });
  },

  selectSpec(e) {
    const { type, val } = e.currentTarget.dataset;
    const selections = this.data.specSelections;
    selections[type] = val;
    this.setData({ specSelections: selections });
  },

  confirmAddToCart() {
    // 1. 获取当前商品信息
    const product = this.data.specProduct;
    const specs = this.data.specSelections;
    
    // 2. 将商品添加到购物车数据结构
    this.addToCartStorage(product, specs);
    
    // 3. 更新页面显示的购物车数量和总价
    this.addToCartLogic(product.price);

    // 4. 关闭弹窗
    this.closeSpecModal();

    wx.showToast({ title: '已加入购物车', icon: 'success' });
  },

  // 将商品添加到本地存储的购物车中
  addToCartStorage(product, specs) {
    // 获取现有购物车数据
    let cart = wx.getStorageSync('cart') || [];
    
    // 生成规格字符串
    const specStr = `${specs.temp}/${specs.sugar}`;
    
    // 生成唯一key，用于区分同商品不同规格
    const uniqueKey = `${product.id}_${specs.temp}_${specs.sugar}`;
    
    // 查找购物车中是否已有该商品+规格
    const existingItemIndex = cart.findIndex(item => item.uniqueKey === uniqueKey);
    
    if (existingItemIndex !== -1) {
      // 已存在，数量+1
      cart[existingItemIndex].quantity += 1;
    } else {
      // 不存在，新增条目
      cart.push({
        id: product.id,
        uniqueKey: uniqueKey,
        name: product.name,
        price: parseFloat(product.price),
        image: product.image,
        specs: specStr,
        quantity: 1
      });
    }
    
    // 保存到本地存储
    wx.setStorageSync('cart', cart);
  },

  // --- 购物车逻辑 ---
  addToCartLogic(price) {
    const newCount = this.data.cartCount + 1;
    const newTotal = this.data.totalPrice + parseFloat(price);

    this.setData({
      cartCount: newCount,
      totalPrice: newTotal,
      cartScale: 'scale-animate'
    });

    // 动画复位
    setTimeout(() => {
      this.setData({ cartScale: '' });
    }, 300);
  },

  goToCheckout() {
    if (this.data.cartCount === 0) {
      wx.showToast({
        title: '购物车是空的，请先添加商品',
        icon: 'none',
        duration: 2000
      });
      return;
    }
    wx.navigateTo({ url: '/pages/orders/checkout/checkout' });
  },

  // --- 抛物线小球动画 (可选功能) ---
  initBalls() {
    const balls = [];
    for (let i = 0; i < 5; i++) {
      balls.push({ inUse: false, id: i });
    }
    this.setData({ balls });
  }
})