import { getCreditLogs } from '../../api/user.js';

Page({
    data: {
        logs: []
    },

    onLoad() {
        this.fetchLogs();
    },

    async fetchLogs() {
        try {
            wx.showLoading();
            const res = await getCreditLogs();

            // 简单格式化时间
            const formatted = res.map(item => {
                return {
                    ...item,
                    createTime: item.createTime.replace('T', ' ').substring(0, 19)
                }
            });

            this.setData({ logs: formatted });
            wx.hideLoading();
        } catch (err) {
            wx.hideLoading();
        }
    }
});