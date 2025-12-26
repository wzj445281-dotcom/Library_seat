const app = getApp();
const productApi = require('../../api/product.js'); // 引入API

// 使用带缓存的请求工具，降低延迟
const { request } = require('../../utils/request-cached');

Page({
  data: {
    loading: true,
    categories: [],
    products: [],
    allProducts: [], // 保存所有商品数据，用于搜索过滤
    activeCategory: 0,
    scrollIntoView: '',

    // --- 购物车相关 ---
    cartCount: 0,
    totalPrice: 0,
    cartScale: '',

    // --- 抛物线动画相关 ---
    balls: [
      { inUse: false, id: 0, styleOuter: '', styleInner: '' },
      { inUse: false, id: 1, styleOuter: '', styleInner: '' },
      { inUse: false, id: 2, styleOuter: '', styleInner: '' },
      { inUse: false, id: 3, styleOuter: '', styleInner: '' },
      { inUse: false, id: 4, styleOuter: '', styleInner: '' }
    ],

    // 当前门店ID，用于判断是否需要刷新
    currentStoreId: null,
    
    // --- 收藏相关 ---
    favoriteMap: {}, // 用对象存储收藏状态，key为productId，value为true，O(1)查找
    
    // --- 搜索相关 ---
    searchKeyword: '', // 搜索关键词
    showSearchHistory: false, // 是否显示搜索历史
    searchHistory: [], // 搜索历史记录
    isSearching: false, // 是否在搜索状态
  },

  cartPos: { x: 40, y: 0 },

  onLoad(options) {
    // 首次加载
    // 注意：Tab页的 onLoad 只会执行一次，数据刷新逻辑主要放在 onShow
  },

  onShow() {
    // 检查登录状态（不再强制跳转，允许未登录浏览）
    const token = wx.getStorageSync('token');
    
    this.checkStoreAndLoadData();
    this.updateCartFromStorage(); // 每次显示页面时同步购物车状态
    
    // 每次显示页面时，刷新收藏状态（保证从详情页返回时状态同步）
    // 只有登录状态下才加载收藏
    if (token) {
      this.fetchFavoriteIds();
    }
    
    // 加载搜索历史
    this.loadSearchHistory();
  },

  onReady() {
    this.queryCartLocation();
  },

  // 检查门店是否变化，并加载数据
  checkStoreAndLoadData() {
    const store = wx.getStorageSync('currentStore');
    const storeId = store ? store.id : 1; // 默认门店ID 1

    // 如果没有门店信息，或者门店变了，或者是首次加载
    if (!this.data.currentStoreId || this.data.currentStoreId !== storeId) {
      this.setData({
        currentStoreId: storeId,
        loading: true
      });

      this.loadMenuData(storeId);
    }
  },

  // 加载真实菜单数据
  loadMenuData(storeId) {
    productApi.getStoreMenu(storeId).then(res => {
      // 假设后端返回结构: { code: 200, data: [ { categoryName: '咖啡', items: [...] } ] }
      // 如果后端接口尚未就绪，这里会报错，我们可以暂时加一个 catch 来使用兜底 Mock 数据
      if (res && res.code === 200) {
        this.transformAndSetData(res.data);
      } else {
        // 接口异常或非200，使用兜底数据
        console.warn('菜单接口调用失败，使用Mock数据');
        this.mockData();
      }
    }).catch(err => {
      console.error('菜单请求网络错误', err);
      this.mockData(); // 网络错误也使用 Mock 数据兜底，保证演示效果
    }).finally(() => {
      // 确保 loading 至少显示一小会儿，避免闪烁
      setTimeout(() => {
        this.setData({ loading: false }, () => {
          this.queryCartLocation();
        });
      }, 500);
    });
  },

  // 将后端数据转换为前端 UI 需要的格式
  transformAndSetData(backendData) {
    // 后端返回的是商品列表，需要按分类分组
    if (!backendData || !Array.isArray(backendData) || backendData.length === 0) {
      console.warn('后端返回数据为空，使用Mock数据');
      this.mockData();
      return;
    }

    // 按 categoryId 分组商品
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
      
      // 格式化商品数据
      // 处理图片路径：如果是 /static/ 开头，转换为完整 URL
      let imageUrl = product.imgUrl || product.image || '';
      if (imageUrl && imageUrl.startsWith('/static/')) {
        // 将 /static/ 路径转换为完整 URL
        const BASE_URL = 'http://localhost:8080';
        imageUrl = BASE_URL + imageUrl;
      } else if (!imageUrl) {
        // 如果没有图片，使用默认占位图
        imageUrl = 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200&fit=crop';
      }
      
      categoryMap[categoryId].items.push({
        id: product.id,
        name: product.name || '未知商品',
        desc: product.description || '',
        price: product.price || 0,
        image: imageUrl
      });
    });

    // 转换为数组并排序
    const products = Object.values(categoryMap).sort((a, b) => a.id - b.id);
    const categories = products.map(cat => cat.name);

    // 保存所有商品数据（扁平化）用于搜索
    const allProducts = [];
    products.forEach(cat => {
      cat.items.forEach(item => {
        allProducts.push({
          ...item,
          categoryId: cat.id,
          categoryName: cat.name
        });
      });
    });

    this.setData({
      categories,
      products,
      allProducts // 保存所有商品用于搜索
    });
  },

  // 兜底 Mock 数据 (保持原有逻辑，以防后端没通)
  mockData() {
    const categories = ['人气Top', '生椰家族', '大师咖啡', '瑞纳冰', '烘焙轻食', '经典饮品'];
    const products = [];

    categories.forEach((cat, index) => {
      const items = [];
      for (let i = 0; i < 4; i++) {
        items.push({
          id: `${index}-${i}`,
          name: `${cat} - 产品${i+1}`,
          desc: '香醇浓郁，回味无穷',
          price: (18 + i * 2),
          image: 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200&fit=crop'
        });
      }
      products.push({
        id: `cat-${index}`,
        name: cat,
        items: items
      });
    });

    // 保存所有商品数据（扁平化）用于搜索
    const allProducts = [];
    products.forEach(cat => {
      cat.items.forEach(item => {
        allProducts.push({
          ...item,
          categoryId: cat.id,
          categoryName: cat.name
        });
      });
    });

    this.setData({
      categories,
      products,
      allProducts // 保存所有商品用于搜索
    });
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

  addToCart(e) {
    const product = e.currentTarget.dataset.item;
    this.runParabola(e);

    // 调用购物车逻辑 (此处仅为前端 UI 更新，后续需对接后端 addCart)
    setTimeout(() => {
      this.updateCartData(product);
    }, 500);
  },

  updateCartData(product) {
    // 简单的前端购物车统计
    this.setData({
      cartCount: this.data.cartCount + 1,
      totalPrice: this.data.totalPrice + product.price,
      cartScale: 'scale-animate'
    });

    setTimeout(() => {
      this.setData({ cartScale: '' });
    }, 200);

    // 实际项目中，这里应该调用 App 实例或 Storage 保存购物车数据
    this.saveCartToStorage();
  },

  saveCartToStorage() {
    // 简易保存，用于 Tab 切换保持状态
    wx.setStorageSync('cart_temp', {
      count: this.data.cartCount,
      total: this.data.totalPrice
    });
  },

  updateCartFromStorage() {
    const cart = wx.getStorageSync('cart_temp');
    if (cart) {
      this.setData({
        cartCount: cart.count,
        totalPrice: cart.total
      });
    }
  },

  runParabola(e) {
    const touch = e.touches[0];
    const startX = touch.clientX;
    const startY = touch.clientY;
    const endX = this.cartPos.x;
    const endY = this.cartPos.y;

    const balls = this.data.balls;
    const index = balls.findIndex(b => !b.inUse);

    if (index === -1) return;

    balls[index].inUse = true;
    balls[index].styleOuter = `left: ${startX}px; top: ${startY}px; transition: none; transform: translate3d(0,0,0);`;
    balls[index].styleInner = `transition: none; transform: translate3d(0,0,0);`;

    this.setData({ balls });

    setTimeout(() => {
      const diffX = endX - startX;
      const diffY = endY - startY;
      balls[index].styleOuter = `left: ${startX}px; top: ${startY}px; transition: all 0.5s cubic-bezier(0.5, -0.2, 1, 1); transform: translate3d(0, ${diffY}px, 0); opacity: 0.5;`;
      balls[index].styleInner = `transition: all 0.5s linear; transform: translate3d(${diffX}px, 0, 0);`;
      this.setData({ balls });
    }, 30);

    setTimeout(() => {
      balls[index].inUse = false;
      this.setData({ balls });
    }, 550);
  },

  goToCheckout() {
    if (this.data.cartCount === 0) return;
    wx.navigateTo({
      url: '/pages/orders/checkout/checkout',
    });
  },

  /**
   * 1. 获取用户所有收藏的商品ID
   * 接口: GET /api/app/favorite/ids
   */
  fetchFavoriteIds() {
    request.get('/app/favorite/ids').then(res => {
      if (res.code === 200) {
        // 将数组转换为 Map 结构 {101: true, 102: true}，方便 WXML 判断
        const map = {};
        res.data.forEach(id => {
          map[id] = true;
        });
        
        this.setData({
          favoriteMap: map
        });
      }
    }).catch(err => {
      console.error('获取收藏列表失败', err);
    });
  },

  /**
   * 2. 核心交互：点击爱心收藏/取消
   * 接口: POST /api/app/favorite/toggle
   */
  onToggleFavorite(e) {
    // 震动反馈，提升手感 (瑞幸风格)
    wx.vibrateShort({ type: 'light' });

    const { id, index, categoryIndex } = e.currentTarget.dataset;
    
    // === 乐观更新 (Optimistic UI) ===
    // 不等接口返回，直接修改前端状态，让用户感觉"零延迟"
    const isFavorite = !!this.data.favoriteMap[id];
    const newStatus = !isFavorite;
    
    const key = `favoriteMap.${id}`;
    this.setData({
      [key]: newStatus
    });

    // 发送请求
    request.post('/app/favorite/toggle', { productId: id }).then(res => {
      if (res.code !== 200) {
        // 如果失败，回滚状态
        this.setData({ [key]: isFavorite });
        wx.showToast({ title: '操作失败', icon: 'none' });
      }
    }).catch(() => {
      // 网络错误回滚
      this.setData({ [key]: isFavorite });
    });
  },

  // ========== 搜索相关方法 ==========
  
  /**
   * 加载搜索历史
   */
  loadSearchHistory() {
    const history = wx.getStorageSync('searchHistory') || [];
    this.setData({
      searchHistory: history.slice(0, 10) // 最多显示10条
    });
  },

  /**
   * 保存搜索历史
   */
  saveSearchHistory(keyword) {
    if (!keyword || keyword.trim() === '') return;
    
    let history = wx.getStorageSync('searchHistory') || [];
    // 移除重复项
    history = history.filter(item => item !== keyword);
    // 添加到开头
    history.unshift(keyword);
    // 最多保存20条
    history = history.slice(0, 20);
    
    wx.setStorageSync('searchHistory', history);
    this.setData({
      searchHistory: history.slice(0, 10)
    });
  },

  /**
   * 搜索输入
   */
  onSearchInput(e) {
    const keyword = e.detail.value;
    this.setData({
      searchKeyword: keyword
    });
    
    // 实时搜索
    if (keyword.trim()) {
      this.performSearch(keyword);
    } else {
      // 清空搜索，恢复原始数据
      this.clearSearch();
    }
  },

  /**
   * 搜索框获得焦点
   */
  onSearchFocus() {
    this.setData({
      showSearchHistory: true
    });
  },

  /**
   * 搜索框失去焦点
   */
  onSearchBlur() {
    // 延迟隐藏，让点击历史记录有时间执行
    setTimeout(() => {
      this.setData({
        showSearchHistory: false
      });
    }, 200);
  },

  /**
   * 搜索确认（点击搜索按钮或回车）
   */
  onSearchConfirm(e) {
    const keyword = e.detail.value || this.data.searchKeyword;
    if (keyword && keyword.trim()) {
      this.performSearch(keyword.trim());
      this.saveSearchHistory(keyword.trim());
      this.setData({
        showSearchHistory: false
      });
    }
  },

  /**
   * 执行搜索
   */
  performSearch(keyword) {
    if (!keyword || keyword.trim() === '') {
      this.clearSearch();
      return;
    }

    const { allProducts } = this.data;
    if (!allProducts || allProducts.length === 0) {
      wx.showToast({
        title: '暂无商品数据',
        icon: 'none'
      });
      return;
    }

    // 搜索过滤：匹配商品名称或描述
    const keywordLower = keyword.toLowerCase();
    const filteredProducts = allProducts.filter(product => {
      const name = (product.name || '').toLowerCase();
      const desc = (product.desc || '').toLowerCase();
      return name.includes(keywordLower) || desc.includes(keywordLower);
    });

    // 按分类分组
    const categoryMap = {};
    filteredProducts.forEach(product => {
      const categoryId = product.categoryId || 0;
      const categoryName = product.categoryName || '搜索结果';
      
      if (!categoryMap[categoryId]) {
        categoryMap[categoryId] = {
          id: categoryId,
          name: categoryName,
          items: []
        };
      }
      
      categoryMap[categoryId].items.push(product);
    });

    const products = Object.values(categoryMap);
    const categories = products.map(cat => cat.name);

    this.setData({
      products,
      categories,
      activeCategory: 0,
      isSearching: true,
      scrollIntoView: ''
    });

    if (filteredProducts.length === 0) {
      wx.showToast({
        title: '未找到相关商品',
        icon: 'none'
      });
    }
  },

  /**
   * 清空搜索
   */
  clearSearch() {
    // 恢复原始数据
    this.loadMenuData(this.data.currentStoreId || 1);
    this.setData({
      searchKeyword: '',
      isSearching: false,
      showSearchHistory: false
    });
  },

  /**
   * 选择搜索历史
   */
  selectHistory(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({
      searchKeyword: keyword,
      showSearchHistory: false
    });
    this.performSearch(keyword);
    this.saveSearchHistory(keyword);
  },

  /**
   * 清除搜索历史
   */
  clearSearchHistory() {
    wx.showModal({
      title: '提示',
      content: '确定要清除所有搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('searchHistory');
          this.setData({
            searchHistory: []
          });
          wx.showToast({
            title: '已清除',
            icon: 'success'
          });
        }
      }
    });
  }
});