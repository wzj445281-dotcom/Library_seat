import { getMyReservations, checkIn, cancelReservation } from '../../api/reservation.js';

Page({
    data: {
        userName: '',
        creditScore: 100, // 暂时写死，后续可接接口
        list: []
    },

    onShow() {
        this.setData({
            userName: wx.getStorageSync('userName')
        });
        this.fetchData();
    },

    // 获取列表
    async fetchData() {
        try {
            const list = await getMyReservations();

            // 处理一下状态显示 (RESERVED -> 已预约)
            const formattedList = list.map(item => {
                let statusStr = item.status;
                if (item.status === 'RESERVED') statusStr = '已预约';
                if (item.status === 'CHECKED_IN') statusStr = '已签到';
                if (item.status === 'CANCELLED') statusStr = '已取消';
                if (item.status === 'COMPLETED') statusStr = '已结束';

                // 简单处理时间显示，去掉 'T'
                item.startTime = item.startTime ? item.startTime.replace('T', ' ') : '';
                item.endTime = item.endTime ? item.endTime.replace('T', ' ') : '';

                return { ...item, statusStr };
            });

            this.setData({ list: formattedList });
        } catch (err) {
            console.error(err);
        }
    },

    // 签到
    async handleCheckIn(e) {
        const id = e.currentTarget.dataset.id;
        try {
            wx.showLoading();
            await checkIn(id);
            wx.hideLoading();
            wx.showToast({ title: '签到成功 +1分' });
            this.fetchData(); // 刷新列表
        } catch (err) {
            wx.hideLoading();
        }
    },

    // 取消
    async handleCancel(e) {
        const id = e.currentTarget.dataset.id;
        wx.showModal({
            title: '提示',
            content: '确定取消吗？临期取消会扣分哦',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        await cancelReservation(id);
                        wx.showToast({ title: '已取消' });
                        this.fetchData();
                    } catch (err) {}
                }
            }
        });
    },

    handleLogout() {
        wx.removeStorageSync('token');
        wx.reLaunch({ url: '/pages/login/login' });
    }
});