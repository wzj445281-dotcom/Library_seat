import request from '../../utils/request.js';

Page({
    data: {
        activeTab: 0,
        searchKey: '',
        list: [],
        cartList: [], // 购物车数据
        cartCount: 0, // 购物车总数
        showCartDetail: false, // 是否展开购物车详情
        page: 1
    },

    onShow() {
        this.fetchList();
        this.fetchCart(); // 每次显示页面都刷新购物车
    },

    // ... 搜索和Tab切换逻辑保持不变 ...
    handleSearchInput(e) { this.setData({ searchKey: e.detail.value }); },
    doSearch() { this.fetchList(); },
    switchTab(e) {
        const idx = parseInt(e.currentTarget.dataset.idx);
        if (this.data.activeTab === idx) return;
        this.setData({ activeTab: idx, searchKey: '' });
        this.fetchList();
    },

    async fetchList() {
        try {
            const res = await request('/app/resources/list', 'GET', {
                type: this.data.activeTab === 1 ? 2 : 0,
                keyword: this.data.searchKey
            });
            this.setData({ list: res || [] });
        } catch (e) { console.error(e); }
    },

    // === 新增：购物车逻辑 ===

    // 1. 获取购物车
    async fetchCart() {
        try {
            const res = await request('/app/resources/cart/list', 'GET');
            this.setData({
                cartList: res || [],
                cartCount: (res || []).length
            });
        } catch (e) { console.error(e); }
    },

    // 2. 加入购物车
    async handleAddToCart(e) {
        const item = e.currentTarget.dataset.item;
        if (item.stock <= 0) return wx.showToast({ title: '库存不足', icon: 'none' });

        try {
            await request('/app/resources/cart/add', 'POST', { resourceId: item.id });
            wx.showToast({ title: '已加入借阅篮', icon: 'none' });
            this.fetchCart(); // 刷新购物车状态
        } catch (err) { }
    },

    // 3. 移出购物车 (在购物车详情里操作)
    async handleRemoveFromCart(e) {
        const item = e.currentTarget.dataset.item;
        try {
            await request('/app/resources/cart/remove', 'POST', { resourceId: item.id });
            this.fetchCart();
            // 如果删空了，自动关闭详情
            if (this.data.cartCount <= 1) {
                this.setData({ showCartDetail: false });
            }
        } catch (err) { }
    },

    // 4. 切换购物车详情显示
    toggleCart() {
        if (this.data.cartCount > 0) {
            this.setData({ showCartDetail: !this.data.showCartDetail });
        }
    },

    // 5. 批量提交订单
    handleSubmitCart() {
        if (this.data.cartCount === 0) return;

        const that = this;
        wx.showActionSheet({
            itemList: ['自取 (Resource Center)', '配送到座 (需已签到)'],
            success(res) {
                const deliveryType = res.tapIndex;
                that.doSubmit(deliveryType);
            }
        });
    },

    async doSubmit(deliveryType) {
        try {
            wx.showLoading({ title: '提交中...' });
            const res = await request('/app/resources/cart/submit', 'POST', { deliveryType });
            wx.hideLoading();

            wx.showModal({
                title: '借阅成功',
                content: `成功借阅 ${res.length} 件物品，请及时取用。`,
                showCancel: false,
                success: () => {
                    this.setData({ showCartDetail: false });
                    this.fetchCart();
                    this.fetchList(); // 刷新库存
                }
            });
        } catch (err) {
            wx.hideLoading();
            // request.js 会自动弹窗报错信息
        }
    }
});