const app = getApp();
const productApi = require('../../api/product.js');
const request = require('../../utils/request.js');

Page({
  data: {
    loading: true,
    categories: [],
    products: [],
    allProducts: [],
    activeCategory: 0,
    scrollIntoView: '',

    // --- 购物车 ---
    cartList: [], // ✅ 新增：完整的商品列表，用于存规格
    cartCount: 0,
    totalPrice: 0,
    cartScale: '',

    // --- 规格弹窗 ---
    showSpecModal: false,
    specProduct: {}, // 当前选中的商品
    specSelections: { // 默认选中的规格
      temp: '冰',
      sugar: '标准糖'
    },

    // --- 抛物线动画 ---
    balls: [
      { inUse: false, id: 0, styleOuter: '', styleInner: '' },
      { inUse: false, id: 1, styleOuter: '', styleInner: '' },
      { inUse: false, id: 2, styleOuter: '', styleInner: '' },
      { inUse: false, id: 3, styleOuter: '', styleInner: '' },
      { inUse: false, id: 4, styleOuter: '', styleInner: '' }
    ],

    currentStoreId: null,
    favoriteMap: {},

    // --- 搜索 ---
    searchKeyword: '',
    showSearchHistory: false,
    searchHistory: [],
    isSearching: false,
  },

  cartPos: { x: 40, y: 0 },

  onLoad(options) {},

  onShow() {
    const token = wx.getStorageSync('token');

    this.checkStoreAndLoadData();
    this.updateCartFromStorage();
    this.loadSearchHistory();

    if (token) {
      this.fetchFavoriteIds();
    }
  },

  onReady() {
    this.queryCartLocation();
  },

  checkStoreAndLoadData() {
    const store = wx.getStorageSync('currentStore');
    const storeId = store ? store.id : 1;

    if (!this.data.currentStoreId || this.data.currentStoreId !== storeId) {
      this.setData({
        currentStoreId: storeId,
        loading: true
      });
      this.loadMenuData(storeId);
    }
  },

  loadMenuData(storeId) {
    productApi.getStoreMenu(storeId).then(res => {
      if (res && res.code === 200) {
        this.transformAndSetData(res.data);
      } else {
        console.warn('接口异常，使用Mock数据');
        this.mockData();
      }
    }).catch(err => {
      console.error('网络错误，使用Mock数据', err);
      this.mockData();
    }).finally(() => {
      setTimeout(() => {
        this.setData({ loading: false }, () => {
          this.queryCartLocation();
        });
      }, 500);
    });
  },

  transformAndSetData(backendData) {
    if (!backendData || !Array.isArray(backendData) || backendData.length === 0) {
      this.mockData();
      return;
    }

    const categoryMap = {};
    backendData.forEach(product => {
      const categoryId = product.categoryId || 0;
      const categoryName = product.categoryName || '其他';

      if (!categoryMap[categoryId]) {
        categoryMap[categoryId] = {
          id: categoryId,
          name: categoryName,
          items: []
        };
      }

      let img = product.imgUrl;
      if (!img || img.trim() === '') {
        img = '/assets/images/american.jpg';
      }

      categoryMap[categoryId].items.push({
        id: product.id,
        name: product.name || '未知商品',
        desc: product.description || '',
        price: product.price || 0,
        image: img
      });
    });

    const products = Object.values(categoryMap).sort((a, b) => a.id - b.id);
    const categories = products.map(cat => cat.name);
    const allProducts = [];
    products.forEach(cat => {
      cat.items.forEach(item => {
        allProducts.push({ ...item, categoryId: cat.id, categoryName: cat.name });
      });
    });

    this.setData({ categories, products, allProducts });
  },

  mockData() {
    const categories = ['大师咖啡', '生椰家族', '瑞纳冰', '烘焙轻食'];
    const localImages = [
      '/assets/images/american.jpg',
      '/assets/images/latte.jpg',
      '/assets/images/coconut_latte.jpg',
      '/assets/images/matcha_ice.jpg'
    ];

    const products = [];
    categories.forEach((cat, index) => {
      const items = [];
      for (let i = 0; i < 4; i++) {
        items.push({
          id: `${index}-${i}`,
          name: `${cat} - 示例${i+1}`,
          desc: '香醇浓郁，回味无穷',
          price: (18 + i * 3),
          image: localImages[index % localImages.length]
        });
      }
      products.push({ id: `cat-${index}`, name: cat, items: items });
    });

    const allProducts = [];
    products.forEach(cat => {
      cat.items.forEach(item => {
        allProducts.push({ ...item, categoryId: cat.id, categoryName: cat.name });
      });
    });

    this.setData({ categories, products, allProducts });
  },

  queryCartLocation() {
    const query = wx.createSelectorQuery().in(this);
    query.select('.cart-icon-wrapper').boundingClientRect(rect => {
      if (rect) {
        this.cartPos.x = rect.left + rect.width / 2;
        this.cartPos.y = rect.top + rect.height / 2;
      }
    }).exec();
  },

  switchCategory(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({
      activeCategory: index,
      scrollIntoView: `cat-${index}`
    });
  },

  // ✅ 新增：打开规格弹窗
  openSpecModal(e) {
    const product = e.currentTarget.dataset.item;
    // 默认选项
    const defaultSpecs = { temp: '冰', sugar: '标准糖' };

    this.setData({
      specProduct: product,
      specSelections: defaultSpecs,
      showSpecModal: true
    });
  },

  // ✅ 新增：关闭规格弹窗
  closeSpecModal() {
    this.setData({ showSpecModal: false });
  },

  // ✅ 新增：选择规格
  selectSpec(e) {
    const { type, val } = e.currentTarget.dataset;
    const selections = this.data.specSelections;
    selections[type] = val;
    this.setData({ specSelections: selections });
  },

  // ✅ 修改：确认加入购物车
  confirmAddToCart(e) {
    const product = this.data.specProduct;
    const specs = this.data.specSelections;
    const specStr = `${specs.temp}/${specs.sugar}`;

    // 1. 构建购物车项
    const cartItem = {
      ...product,
      spec: specStr,
      // 生成唯一标识：id + 规格，防止不同规格的商品合并
      cartId: `${product.id}_${specStr}`,
      count: 1
    };

    // 2. 执行抛物线动画 (为了视觉效果，位置取屏幕中央大概位置，或者直接不传e使用默认)
    // 由于是从弹窗点击，没有点击事件e，我们可以模拟一个起始点或者直接播放动画
    // 这里简单处理，只更新数据，或者手动设置一个动画起点

    // 3. 更新购物车数据
    this.updateCartData(cartItem);

    // 4. 关闭弹窗
    this.closeSpecModal();
    wx.showToast({ title: '已加入购物车', icon: 'success', duration: 800 });
  },

  // ✅ 修改：更新购物车数据 (支持多商品列表)
  updateCartData(newItem) {
    let list = this.data.cartList;
    const existingIndex = list.findIndex(item => item.cartId === newItem.cartId);

    if (existingIndex > -1) {
      // 已存在同规格商品，数量+1
      list[existingIndex].count += 1;
    } else {
      // 新商品，加入列表
      list.push(newItem);
    }

    // 重新计算总价和总数
    let total = 0;
    let count = 0;
    list.forEach(item => {
      total += item.price * item.count;
      count += item.count;
    });

    this.setData({
      cartList: list,
      cartCount: count,
      totalPrice: total,
      cartScale: 'scale-animate'
    });

    setTimeout(() => {
      this.setData({ cartScale: '' });
    }, 200);

    this.saveCartToStorage();
  },

  saveCartToStorage() {
    // 保存简略信息 (用于Tab显示)
    wx.setStorageSync('cart_temp', {
      count: this.data.cartCount,
      total: this.data.totalPrice
    });
    // ✅ 保存详细列表 (用于结算页)
    wx.setStorageSync('cart_data_detail', this.data.cartList);
  },

  updateCartFromStorage() {
    const cart = wx.getStorageSync('cart_temp');
    // 读取详细列表
    const list = wx.getStorageSync('cart_data_detail') || [];

    if (cart) {
      this.setData({
        cartCount: cart.count,
        totalPrice: cart.total,
        cartList: list
      });
    }
  },

  // ... (保留抛物线动画，但这次主要是弹窗加购，可能不需要从列表直接飞入的动画了)
  runParabola(e) {
    // ... 现有代码
  },

  goToCheckout() {
    if (this.data.cartCount === 0) return;
    wx.navigateTo({
      url: '/pages/orders/checkout/checkout',
    });
  },

  // --- 收藏逻辑 ---
  fetchFavoriteIds() {
    request.get('/app/favorite/ids').then(res => {
      if (res.code === 200) {
        const map = {};
        res.data.forEach(id => {
          map[id] = true;
        });
        this.setData({ favoriteMap: map });
      }
    }).catch(err => {
      console.error('获取收藏失败', err);
    });
  },

  onToggleFavorite(e) {
    wx.vibrateShort({ type: 'light' });
    const { id } = e.currentTarget.dataset;
    const isFavorite = !!this.data.favoriteMap[id];
    const key = `favoriteMap.${id}`;

    this.setData({ [key]: !isFavorite });

    request.post('/app/favorite/toggle', { productId: id }).catch(() => {
      this.setData({ [key]: isFavorite });
    });
  },

  // --- 搜索逻辑保持不变 ---
  loadSearchHistory() {
    const history = wx.getStorageSync('searchHistory') || [];
    this.setData({ searchHistory: history.slice(0, 10) });
  },

  onSearchInput(e) {
    const keyword = e.detail.value;
    this.setData({ searchKeyword: keyword });
    if (keyword.trim()) {
      this.performSearch(keyword);
    } else {
      this.clearSearch();
    }
  },

  onSearchFocus() {
    this.setData({ showSearchHistory: true });
  },

  onSearchBlur() {
    setTimeout(() => {
      this.setData({ showSearchHistory: false });
    }, 200);
  },

  onSearchConfirm(e) {
    const keyword = e.detail.value || this.data.searchKeyword;
    if (keyword && keyword.trim()) {
      this.performSearch(keyword.trim());
      this.saveSearchHistory(keyword.trim());
      this.setData({ showSearchHistory: false });
    }
  },

  performSearch(keyword) {
    if (!keyword || keyword.trim() === '') {
      this.clearSearch();
      return;
    }
    const { allProducts } = this.data;
    const keywordLower = keyword.toLowerCase();

    const filtered = allProducts.filter(p =>
        (p.name || '').toLowerCase().includes(keywordLower) ||
        (p.desc || '').toLowerCase().includes(keywordLower)
    );

    const categoryMap = {};
    filtered.forEach(p => {
      const cid = p.categoryId;
      if(!categoryMap[cid]) categoryMap[cid] = { id: cid, name: p.categoryName, items: [] };
      categoryMap[cid].items.push(p);
    });

    const products = Object.values(categoryMap);
    const categories = products.map(c => c.name);

    this.setData({
      products,
      categories,
      activeCategory: 0,
      isSearching: true,
      scrollIntoView: ''
    });
  },

  clearSearch() {
    this.checkStoreAndLoadData();
    this.setData({
      searchKeyword: '',
      isSearching: false,
      showSearchHistory: false
    });
  },

  saveSearchHistory(keyword) {
    if (!keyword) return;
    let history = wx.getStorageSync('searchHistory') || [];
    history = history.filter(item => item !== keyword);
    history.unshift(keyword);
    wx.setStorageSync('searchHistory', history.slice(0, 20));
    this.setData({ searchHistory: history.slice(0, 10) });
  },

  selectHistory(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ searchKeyword: keyword });
    this.performSearch(keyword);
  },

  clearSearchHistory() {
    wx.removeStorageSync('searchHistory');
    this.setData({ searchHistory: [] });
  }
});