import { getUserInfo } from '../../api/user.js';
import request from '../../utils/request.js';

Page({
    data: {
        userName: '',
        creditScore: 100,
        creditLevel: '', // 信用等级
        creditClass: '', // CSS 类名
        list: []
    },

    onShow() {
        this.fetchUserInfo();
        this.fetchMyReservations();
    },

    async fetchUserInfo() {
        try {
            const user = await getUserInfo();
            // 计算信用等级
            let level = '良好';
            let cls = 'good';
            const s = user.creditScore;

            if (s >= 105) { level = '极好'; cls = 'excellent'; }
            else if (s >= 90) { level = '优秀'; cls = 'great'; }
            else if (s >= 70) { level = '良好'; cls = 'good'; }
            else if (s >= 60) { level = '中等'; cls = 'fair'; }
            else { level = '高风险'; cls = 'risk'; }

            this.setData({
                userName: user.name,
                creditScore: s,
                creditLevel: level,
                creditClass: cls
            });
            wx.setStorageSync('userId', user.id);
        } catch (err) {
            console.error(err);
        }
    },

    async fetchMyReservations() {
        try {
            const list = await request('/app/reservation/list', 'GET');
            // 处理状态显示
            const fmtList = list.map(item => {
                let statusStr = '';
                switch(item.status) {
                    case 'RESERVED': statusStr = '已预约'; break;
                    case 'CHECKED_IN': statusStr = '使用中'; break;
                    case 'COMPLETED': statusStr = '已结束'; break;
                    case 'CANCELLED': statusStr = '已取消'; break;
                    case 'VIOLATION': statusStr = '已违约'; break;
                }
                return {
                    ...item,
                    statusStr,
                    // 简单截取时间字符串
                    startTime: item.startTime.replace('T', ' ').substring(5, 16),
                    endTime: item.endTime.replace('T', ' ').substring(11, 16)
                };
            });
            this.setData({ list: fmtList });
        } catch (err) {
            console.error(err);
        }
    },

    // 结束使用 (释放座位)
    handleLeave(e) {
        const id = e.currentTarget.dataset.id;
        wx.showModal({
            title: '结束使用',
            content: '确定要离开座位并释放资源吗？这将有助于提升您的信用记录。',
            confirmColor: '#1296db',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        wx.showLoading({ title: '处理中' });
                        await request(`/app/reservation/leave/${id}`, 'POST');
                        wx.hideLoading();
                        wx.showToast({ title: '已释放，感谢配合', icon: 'success' });
                        this.fetchMyReservations(); // 刷新列表
                    } catch (err) {
                        wx.hideLoading();
                    }
                }
            }
        });
    },

    // 跳转逻辑
    goToCredit() { wx.navigateTo({ url: '/pages/credit/credit' }); },
    goToFeedback() { wx.navigateTo({ url: '/pages/feedback/feedback' }); },
    goToAiChat() { wx.navigateTo({ url: '/pages/ai_chat/ai_chat' }); }, // 新增入口

    handleLogout() {
        wx.removeStorageSync('token');
        wx.reLaunch({ url: '/pages/login/login' });
    }
});