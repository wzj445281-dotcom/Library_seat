import request from '../../utils/request.js';

Page({
    data: {
        points: 0,
        exchangeCount: 0, // 新增：已兑换次数
        rank: '-',        // 新增：排名
        activeTab: 0,
        products: []
    },

    onShow() {
        this.fetchWallet();
        this.fetchProducts();
    },

    async fetchWallet() {
        try {
            const res = await request('/app/wallet/info', 'GET');
            this.setData({
                points: res.pointsBalance,
                // 这里假设后端也返回了这俩字段，如果没有就算了
                exchangeCount: res.exchangeCount || 0,
                rank: res.rank || '前10%'
            });
        } catch (e) {
            console.error(e);
        }
    },

    async fetchProducts() {
        // 0=VIRTUAL (权益), 1=REAL (物资)
        const type = this.data.activeTab === 0 ? 'VIRTUAL' : 'REAL';
        try {
            const res = await request(`/app/mall/products?type=${type}`, 'GET');
            this.setData({ products: res });
        } catch (e) {
            console.error(e);
        }
    },

    switchTab(e) {
        const idx = parseInt(e.currentTarget.dataset.idx);
        if (this.data.activeTab === idx) return;

        this.setData({ activeTab: idx });
        this.fetchProducts();
    },

    handleExchange(e) {
        const prodId = e.currentTarget.dataset.id;
        const product = this.data.products.find(p => p.id === prodId);

        if (this.data.points < product.price) {
            return wx.showToast({ title: '积分不足，继续努力学习吧！', icon: 'none' });
        }

        wx.showModal({
            title: '兑换确认',
            content: `确定使用 ${product.price} 积分兑换\n[${product.name}] 吗？`,
            confirmColor: '#4facfe',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        wx.showLoading({ title: '兑换中...' });
                        await request('/app/mall/exchange', 'POST', { productId: prodId });
                        wx.hideLoading();

                        wx.showToast({ title: '兑换成功！', icon: 'success' });

                        // 刷新数据
                        this.fetchWallet();
                        this.fetchProducts();
                    } catch (err) {
                        wx.hideLoading();
                    }
                }
            }
        });
    }
});