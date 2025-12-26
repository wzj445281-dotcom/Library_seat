const app = getApp();

Page({
    data: {
        isLogin: false,
        userInfo: {
            nickName: '点击登录',
            avatarUrl: '/assets/images/user.png', // 默认头像
            level: 0
        },
        // 资产数据 (登录后显示)
        assets: {
            points: '-',
            couponCount: '-',
            balance: '-'
        },
        // 功能菜单列表
        menuList: [
            {
                id: 'orders',
                icon: '🧾',
                title: '我的订单',
                url: '/pages/orders/list' // 对应 app.json 中的 TabBar 路径
            },
            {
                id: 'address',
                icon: '📍',
                title: '地址管理',
                url: '/pages/address/list'
            },
            {
                id: 'ai',
                icon: '🤖',
                title: 'AI 助手',
                url: '/pages/ai_chat/ai_chat'
            },
            {
                id: 'feedback',
                icon: '❓',
                title: '帮助反馈',
                url: '/pages/feedback/feedback'
            }
        ]
    },

    onShow() {
        this.checkLoginStatus();
    },

    /**
     * 检查登录状态并更新页面数据
     */
    checkLoginStatus() {
        const token = wx.getStorageSync('token');
        const userInfo = wx.getStorageSync('userInfo');

        if (token && userInfo) {
            // 已登录
            this.setData({
                isLogin: true,
                userInfo: {
                    nickName: userInfo.nickName || '瑞幸会员',
                    avatarUrl: userInfo.avatarUrl || '/assets/images/user-active.png',
                    level: userInfo.level || 1
                },
                // 模拟资产数据 (实际项目应从后端获取)
                assets: {
                    points: 128,
                    couponCount: 3,
                    balance: 50.00
                }
            });
        } else {
            // 未登录
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

    /**
     * 点击头部登录
     */
    handleLogin() {
        if (!this.data.isLogin) {
            wx.navigateTo({ url: '/pages/login/login' });
        }
    },

    /**
     * 退出登录
     */
    handleLogout() {
        wx.showModal({
            title: '提示',
            content: '确定要退出登录吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.removeStorageSync('token');
                    wx.removeStorageSync('userInfo');

                    if (app.globalData) {
                        app.globalData.isLogin = false;
                        app.globalData.userInfo = null;
                    }

                    this.checkLoginStatus();
                    wx.showToast({ title: '已退出', icon: 'none' });
                }
            }
        });
    },

    /**
     * 菜单点击处理
     */
    onMenuClick(e) {
        const item = e.currentTarget.dataset.item;

        // 需要登录权限的功能ID
        const authRequired = ['orders', 'address', 'wallet'];

        // 检查登录权限
        if (authRequired.includes(item.id) && !this.data.isLogin) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            setTimeout(() => {
                wx.navigateTo({ url: '/pages/login/login' });
            }, 1000);
            return;
        }

        // 页面跳转
        if (item.url) {
            // 特殊处理 TabBar 页面跳转
            if (item.url === '/pages/orders/list' || item.url === '/pages/menu/index') {
                wx.switchTab({ url: item.url });
            } else {
                wx.navigateTo({
                    url: item.url,
                    fail: (err) => {
                        console.error('跳转失败:', err);
                        wx.showToast({ title: '功能开发中', icon: 'none' });
                    }
                });
            }
        } else {
            wx.showToast({ title: '功能即将上线', icon: 'none' });
        }
    },

    /**
     * 点击资产栏的优惠券
     */
    navigateToCoupon() {
        if (!this.data.isLogin) {
            wx.navigateTo({ url: '/pages/login/login' });
            return;
        }
        // 根据 app.json，优惠券在分包中
        wx.navigateTo({
            url: '/subpackages/coupon/index'
        });
    }
});