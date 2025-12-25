import { getUserInfo } from '../../api/user.js';
import request from '../../utils/request.js';

Page({
    data: {
        userName: '',
        creditScore: 0,
        creditLevel: '普通', // 默认为普通
        stats: {
            totalCups: 0,
            totalSaved: '0.0'
        }
    },

    onShow() {
        this.fetchUserInfo();
        this.fetchUserStats(); // 获取统计数据
    },

    async fetchUserInfo() {
        try {
            const user = await getUserInfo();
            // 根据信用分计算简单的会员等级
            let level = '普通';
            if (user.creditScore > 200) level = '钻石';
            else if (user.creditScore > 100) level = '黄金';

            this.setData({
                userName: user.name || '微信用户',
                creditScore: user.creditScore || 0,
                creditLevel: level
            });

            // 缓存用户ID供其他页面使用
            if (user.id) {
                wx.setStorageSync('userId', user.id);
            }
        } catch (err) {
            console.error('获取用户信息失败', err);
            // 失败时引导登录（可选）
        }
    },

    // 模拟获取用户消费统计
    async fetchUserStats() {
        // 实际开发中，这里应该调用类似 /api/app/user/stats 的接口
        // 这里我们使用模拟数据，或者基于本地缓存计算

        // 模拟延迟
        setTimeout(() => {
            this.setData({
                stats: {
                    totalCups: 12, // 累计杯数
                    totalSaved: '45.0' // 累计节省金额
                }
            });
        }, 500);
    },

    // 页面跳转逻辑
    navigateToOrders() {
        wx.switchTab({ url: '/pages/orders/list' }); // 如果是 tabBar 页面用 switchTab
        // 如果 orders 不是 tabBar 页面，改用:
        // wx.navigateTo({ url: '/pages/orders/list' });
    },

    goToCredit() {
        wx.navigateTo({ url: '/pages/credit/credit' });
    },

    goToFeedback() {
        wx.navigateTo({ url: '/pages/feedback/feedback' });
    },

    goToAiChat() {
        wx.navigateTo({ url: '/pages/ai_chat/ai_chat' });
    },

    handleLogout() {
        wx.showModal({
            title: '提示',
            content: '确定要退出登录吗？',
            confirmColor: '#0022AB',
            success: (res) => {
                if (res.confirm) {
                    wx.removeStorageSync('token');
                    wx.removeStorageSync('userId');
                    wx.reLaunch({ url: '/pages/login/login' });
                }
            }
        });
    }
});