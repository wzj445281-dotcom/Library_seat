Page({
    data: {
        recommends: [
            { id: 1, name: '生椰拿铁', price: '18.00' },
            { id: 2, name: '厚乳拿铁', price: '16.00' },
            { id: 3, name: '丝绒拿铁', price: '19.00' },
            { id: 4, name: '标准美式', price: '13.00' }
        ]
    },

    // 跳转到菜单页
    goToMenu(e) {
        const type = e.currentTarget.dataset.type;
        // 实际项目中可以通过 url 参数传递 1=自提 2=外卖
        wx.switchTab({
            url: '/pages/menu/index'
        });
    },

    goToMine() {
        wx.switchTab({
            url: '/pages/mine/mine'
        });
    },

    onShareAppMessage() {
        return {
            title: '瑞幸咖啡 - 每一杯都值得',
            path: '/pages/index/index'
        }
    }
});