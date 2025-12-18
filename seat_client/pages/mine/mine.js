import { getMyReservations, checkIn, cancelReservation } from '../../api/reservation.js';
import { getUserInfo } from '../../api/user.js';
import request from '../../utils/request.js';

Page({
    data: {
        userName: '',
        creditScore: 100, // 初始默认值
        list: []
    },

    onShow() {
        // 每次显示页面，都重新拉取数据
        this.fetchUserInfo();
        this.fetchData();
    },

    // 新增：拉取最新用户信息
    async fetchUserInfo() {
        try {
            const user = await getUserInfo();
            this.setData({
                userName: user.name,
                creditScore: user.creditScore
            });
            // 更新本地缓存，备用
            wx.setStorageSync('userId', user.id);
            wx.setStorageSync('userName', user.name);
        } catch (err) {
            console.error('获取用户信息失败', err);
        }
    },

    goToCredit() {
        wx.navigateTo({
            url: '/pages/credit/credit',
        });
    },

    goToFeedback() {
        wx.navigateTo({
            url: '/pages/feedback/feedback',
        });
    },

    async handleLeave(e) {
        const id = e.currentTarget.dataset.id;
        wx.showModal({
            title: '结束使用',
            content: '确定要离开座位并释放资源吗？',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        wx.showLoading();
                        await request(`/app/reservation/leave/${id}`, 'POST');
                        wx.hideLoading();
                        wx.showToast({ title: '座位已释放' });
                        this.fetchData();
                    } catch (err) {
                        wx.hideLoading();
                    }
                }
            }
        });
    },

    handleLogout() {
        wx.removeStorageSync('token');
        wx.reLaunch({ url: '/pages/login/login' });
    }
});