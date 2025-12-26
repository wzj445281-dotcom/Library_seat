const app = getApp();
const authApi = require('../../api/auth.js');

Page({
    data: {
        isLogin: false,
        userInfo: {
            nickName: '点击登录',
            avatarUrl: '/assets/images/user.png', // 默认头像
            level: 0
        },
        // 资产数据 (Mock)
        assets: {
            points: 0,
            couponCount: 0,
            balance: 0.00
        },
        // 功能菜单配置
        menuList: [
            { id: 'orders', icon: '🧾', title: '我的订单', url: '/pages/orders/list', type: 'page' },
            { id: 'address', icon: '📍', title: '地址管理', url: '/pages/address/list', type: 'page' },
            { id: 'service', icon: '🤖', title: 'AI 客服', url: '/pages/ai_chat/ai_chat', type: 'page' },
            { id: 'about', icon: 'ℹ️', title: '关于我们', url: '', type: 'toast', tip: '版本 v1.0.0' }
        ]
    },

    onLoad() {
        // 页面加载时初始化
        this.checkLogin();
    },

    onShow() {
        // 页面显示时刷新登录状态
        this.checkLogin();
    },

    checkLogin() {
        // 从全局或缓存获取登录状态
        const token = wx.getStorageSync('token');
        const user = wx.getStorageSync('userInfo');

        if (token && user) {
            this.setData({
                isLogin: true,
                userInfo: {
                    nickName: user.nickName || '瑞幸会员',
                    avatarUrl: user.avatarUrl || '/assets/images/user-active.png', // 登录后头像
                    level: user.level || 1
                },
                // 模拟已登录用户的资产数据
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
        if (this.data.isLogin) return;

        // 触发全局登录逻辑 (通常会弹窗授权，这里简化为静默/模拟登录)
        wx.showLoading({ title: '登录中...' });

        // 模拟登录过程 (实际应调用 app.doLogin 或跳转登录页)
        setTimeout(() => {
            // 模拟后端返回用户信息
            const mockUser = {
                nickName: '微信用户_888',
                avatarUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
                level: 2
            };

            wx.setStorageSync('token', 'mock-token-123456');
            wx.setStorageSync('userInfo', mockUser);

            this.checkLogin(); // 刷新页面状态
            wx.hideLoading();
            wx.showToast({ title: '欢迎回来', icon: 'success' });
        }, 1000);
    },

    handleLogout() {
        wx.showModal({
            title: '提示',
            content: '确定要退出登录吗？',
            success: (res) => {
                if (res.confirm) {
                    wx.removeStorageSync('token');
                    wx.removeStorageSync('userInfo');
                    this.checkLogin(); // 恢复未登录状态
                    authApi.logout().catch(() => {}); // 通知后端
                }
            }
        });
    },

    onMenuClick(e) {
        const item = e.currentTarget.dataset.item;

        // 某些功能需要登录
        if (['orders', 'address'].includes(item.id) && !this.data.isLogin) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            return;
        }

        if (item.type === 'page' && item.url) {
            wx.navigateTo({ url: item.url });
        } else if (item.type === 'toast') {
            wx.showToast({ title: item.tip || '敬请期待', icon: 'none' });
        }
    }
});