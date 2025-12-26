const app = getApp();
const productApi = require('../../api/product.js');

// ✅ 【核心修复】使用基础 request 工具，避免解构失败导致的 undefined 错误
const request = require('../../utils/request.js');

Page({
    data: {
        loading: true,
        categories: [],
        products: [],
        allProducts: [], // 用于搜索
        activeCategory: 0,
        scrollIntoView: '',

        // --- 购物车 ---
        cartCount: 0,
        totalPrice: 0,
        cartScale: '',

        // --- 抛物线小球 ---
        balls: [
            { inUse: false, id: 0, styleOuter: '', styleInner: '' },
            { inUse: false, id: 1, styleOuter: '', styleInner: '' },
            { inUse: false, id: 2, styleOuter: '', styleInner: '' },
            { inUse: false, id: 3, styleOuter: '', styleInner: '' },
            { inUse: false, id: 4, styleOuter: '', styleInner: '' }
        ],

        currentStoreId: null,
        favoriteMap: {}, // 收藏状态缓存

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

        // 1. 加载菜单数据
        this.checkStoreAndLoadData();

        // 2. 同步购物车状态
        this.updateCartFromStorage();

        // 3. 加载收藏状态 (仅登录时)
        if (token) {
            this.fetchFavoriteIds();
        }

        // 4. 加载搜索历史
        this.loadSearchHistory();
    },

    onReady() {
        // 获取购物车图标位置，用于动画
        this.queryCartLocation();
    },

    // --- 数据加载逻辑 ---
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

    // 数据格式化
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

            // 处理图片：如果后端返回空，使用默认图
            let img = product.imgUrl;
            if (!img || img.includes('alicdn')) { // 过滤掉可能的坏链
                img = 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200&fit=crop';
            }

            categoryMap[categoryId].items.push({
                id: product.id,
                name: product.name || '未知商品',
                desc: product.description || '',
                price: product.price || 0,
                image: img
            });
        });

        // 转数组并排序
        const products = Object.values(categoryMap).sort((a, b) => a.id - b.id);
        const categories = products.map(cat => cat.name);

        // 扁平化数据用于搜索
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

        this.setData({ categories, products, allProducts });
    },

    // 兜底 Mock 数据
    mockData() {
        const categories = ['人气Top', '生椰家族', '大师咖啡', '瑞纳冰'];
        const products = [];

        categories.forEach((cat, index) => {
            const items = [];
            for (let i = 0; i < 4; i++) {
                items.push({
                    id: `${index}-${i}`,
                    name: `${cat} - 示例${i+1}`,
                    desc: '香醇浓郁',
                    price: (18 + i * 2),
                    image: 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200&fit=crop'
                });
            }
            products.push({ id: `cat-${index}`, name: cat, items: items });
        });

        // 同步构造 allProducts
        const allProducts = [];
        products.forEach(cat => {
            cat.items.forEach(item => {
                allProducts.push({ ...item, categoryId: cat.id, categoryName: cat.name });
            });
        });

        this.setData({ categories, products, allProducts });
    },

    // --- 交互逻辑 ---
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

        setTimeout(() => {
            this.updateCartData(product);
        }, 500);
    },

    updateCartData(product) {
        this.setData({
            cartCount: this.data.cartCount + 1,
            totalPrice: this.data.totalPrice + product.price,
            cartScale: 'scale-animate'
        });

        setTimeout(() => {
            this.setData({ cartScale: '' });
        }, 200);

        this.saveCartToStorage();
    },

    saveCartToStorage() {
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

    // --- 收藏逻辑 (修复 Cannot read property 'get' 处) ---
    fetchFavoriteIds() {
        // ✅ 修复：request 是对象，可以直接调用 .get
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

        // 乐观更新
        this.setData({ [key]: !isFavorite });

        request.post('/app/favorite/toggle', { productId: id }).catch(() => {
            // 失败回滚
            this.setData({ [key]: isFavorite });
        });
    },

    // --- 搜索逻辑 ---
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

        // 重新分组显示
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
        // 重新加载原数据
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