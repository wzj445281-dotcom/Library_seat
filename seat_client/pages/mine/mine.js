const app = getApp();

Page({
    data: {
        isLogin: false,
        userInfo: {
            nickName: '点击登录',
            avatarUrl: '/assets/images/user.png',
            level: 0
        },
        // 资产数据
        assets: {
            points: 0,
            couponCount: 0,
            balance: 0.00
        },
        // 功能菜单配置
        menuList: [
            { id: 'order', icon: '🧾', title: '我的订单', url: '/pages/orders/list' }, // 修正路径
            { id: 'coupon', icon: '🎫', title: '优惠券', url: '/pages/coupon/index' }, // 新增优惠券入口
            { id: 'wallet', icon: '💰', title: '咖啡钱包', url: '' },
            { id: 'address', icon: '📍', title: '地址管理', url: '/pages/address/list' },
            { id: 'help', icon: '❓', title: '帮助反馈', url: '/pages/feedback/feedback' },
            { id: 'ai', icon: '🤖', title: 'AI助手', url: '/pages/ai_chat/ai_chat' }
        ]
    },

    onShow() {
        this.checkLoginStatus();
    },

    checkLoginStatus() {
        const token = wx.getStorageSync('token');
        const userInfo = wx.getStorageSync('userInfo');

        if (token && userInfo) {
            this.setData({
                isLogin: true,
                userInfo: {
                    nickName: userInfo.nickName || '瑞幸会员',
                    avatarUrl: userInfo.avatarUrl || '/assets/images/user-active.png',
                    level: userInfo.level || 1
                },
                // 实际项目中这里应该调用后端 API 获取最新资产数据
                assets: {
                    points: 128,
                    couponCount: 3,
                    balance: 50.00
                }
            });
        } else {
            this.setData({
                isLogin: false,
                userInfo: {
                    nickName: '点击登录',
                    avatarUrl: '/assets/images/user.png',
                    level: 0
                },
                assets: { points: '-', couponCount: '-', balance: '-' }
            });
        }
    },

    handleLogin() {
        if (!this.data.isLogin) {
            wx.navigateTo({ url: '/pages/login/login' });
        }
    },

    handleLogout() {
        wx.showModal({
            title: '提示',
            content: '确定要退出登录吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.clearStorageSync();
                    this.checkLoginStatus();
                    wx.showToast({ title: '已退出', icon: 'none' });
                }
            }
        });
    },

    onMenuClick(e) {
        const item = e.currentTarget.dataset.item;

        // 需要登录权限的功能列表
        const authRequired = ['wallet', 'coupon', 'order', 'address'];

        if (authRequired.includes(item.id) && !this.data.isLogin) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            setTimeout(() => {
                wx.navigateTo({ url: '/pages/login/login' });
            }, 1000);
            return;
        }

        if (item.url) {
            wx.navigateTo({
                url: item.url,
                fail: () => {
                    // 如果是 TabBar 页面，必须用 switchTab
                    wx.switchTab({ url: item.url });
                }
            });
        } else {
            wx.showToast({ title: '功能开发中...', icon: 'none' });
        }
    }
});