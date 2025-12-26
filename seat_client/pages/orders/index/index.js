const app = getApp();
const ProductAPI = require('../../api/product.js');

Page({
    data: {
        categories: [],      // 分类及商品数据
        activeCategory: 0,   // 当前选中的分类索引
        toView: '',          // 滚动锚点
        cart: [],            // 购物车数据
        totalPrice: 0,       // 总价
        totalCount: 0,       // 总数量
        showCartDetail: false, // 是否显示购物车详情
        loading: true
    },

    onLoad() {
        this.loadData();
    },

    onShow() {
        // 每次显示页面（包括从结算页返回），同步购物车状态
        this.syncCartStatus();
    },

    /**
     * 加载商品数据
     */
    loadData() {
        this.setData({ loading: true });

        // ✅ 【修复点】这里改为调用 getStoreMenu
        ProductAPI.getStoreMenu()
            .then(res => {
                let categories = [];
                if (res.code === 200) {
                    categories = res.data || [];
                }

                this.setData({ categories, loading: false });

                // 数据加载完后，立即同步一次购物车状态
                this.syncCartStatus();
            })
            .catch(err => {
                console.error(err);
                wx.showToast({ title: '加载菜单失败', icon: 'none' });
                this.setData({ loading: false });
            });
    },

    /**
     * 同步本地缓存到页面显示
     * 1. 更新底部购物车栏（总价、总数）
     * 2. 更新商品列表上的数量角标 (quantity)
     */
    syncCartStatus() {
        // 1. 读取最新缓存
        const cart = wx.getStorageSync('cart') || [];

        // 2. 计算总价和总数
        let total = 0;
        let count = 0;
        cart.forEach(item => {
            total += item.price * item.quantity;
            count += item.quantity;
        });

        // 3. 同步商品列表中的 quantity 显示
        const categories = this.data.categories;
        if (categories && categories.length > 0) {
            // 遍历所有商品，重置 quantity
            categories.forEach(cat => {
                if (cat.products) {
                    cat.products.forEach(prod => {
                        const found = cart.find(c => c.id === prod.id);
                        prod.quantity = found ? found.quantity : 0;
                    });
                }
            });
        }

        // 4. 更新页面数据
        this.setData({
            cart: cart,
            totalPrice: total.toFixed(2),
            totalCount: count,
            categories: categories, // 触发视图更新商品角标
            showCartDetail: cart.length > 0 ? this.data.showCartDetail : false // 如果空了就关掉详情
        });
    },

    // 切换分类
    switchCategory(e) {
        const index = e.currentTarget.dataset.index;
        this.setData({
            activeCategory: index,
            toView: `cat_${index}`
        });
    },

    /**
     * 添加到购物车 (+)
     */
    addToCart(e) {
        const catIndex = e.currentTarget.dataset.catindex;
        const prodIndex = e.currentTarget.dataset.prodindex;
        const product = this.data.categories[catIndex].products[prodIndex];

        this.updateCartItem(product, 1);
    },

    /**
     * 减少商品 (-)
     */
    reduceFromCart(e) {
        const catIndex = e.currentTarget.dataset.catindex;
        const prodIndex = e.currentTarget.dataset.prodindex;
        const product = this.data.categories[catIndex].products[prodIndex];

        this.updateCartItem(product, -1);
    },

    /**
     * 统一处理购物车更新逻辑
     */
    updateCartItem(product, change) {
        let cart = wx.getStorageSync('cart') || [];
        const index = cart.findIndex(c => c.id === product.id);

        if (index > -1) {
            // 购物车里已有，更新数量
            cart[index].quantity += change;
            if (cart[index].quantity <= 0) {
                cart.splice(index, 1); // 减到0则移除
            }
        } else if (change > 0) {
            // 新增
            cart.push({
                id: product.id,
                name: product.name,
                price: product.price,
                pic: product.imgUrl || product.image, // 兼容字段
                quantity: 1,
                spec: '' // 默认规格
            });
        }

        // 保存并同步
        wx.setStorageSync('cart', cart);
        this.syncCartStatus();
    },

    // 显示/隐藏购物车详情
    toggleCart() {
        if (this.data.totalCount > 0) {
            this.setData({ showCartDetail: !this.data.showCartDetail });
        }
    },

    // 清空购物车
    clearCart() {
        wx.showModal({
            title: '提示',
            content: '确定清空购物车吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.removeStorageSync('cart');
                    this.syncCartStatus();
                }
            }
        });
    },

    // 去结算
    goCheckout() {
        if (this.data.totalCount === 0) return;

        // 检查登录
        if (!wx.getStorageSync('token')) {
            wx.navigateTo({ url: '/pages/login/login' });
            return;
        }

        wx.navigateTo({
            url: '/pages/orders/checkout/checkout'
        });
    }
});