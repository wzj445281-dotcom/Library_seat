const app = getApp();

Page({
    data: {
        loading: true, // 控制骨架屏显示
        banners: [
            'https://images.unsplash.com/photo-1509042239860-f550ce710b93?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80',
            'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80'
        ],
        storeInfo: {
            name: '瑞幸咖啡 (科技园店)',
            address: '高新南九道10号',
            distance: '150m'
        }
    },

    onLoad() {
        this.simulateLoading();
    },

    // 模拟数据加载过程，展示骨架屏效果
    simulateLoading() {
        // 真实场景下，这里是在等待 wx.request 的 success 回调
        // 商业级应用通常会设置一个最小展示时间，防止骨架屏一闪而过造成视觉闪烁
        setTimeout(() => {
            this.setData({
                loading: false
            });
        }, 1500); // 模拟1.5秒加载时间
    },

    navigateToMenu(e) {
        const type = e.currentTarget.dataset.type;
        // 将用餐方式存入全局或缓存
        wx.setStorageSync('diningType', type);

        wx.switchTab({
            url: '/pages/menu/index'
        });
    }
});