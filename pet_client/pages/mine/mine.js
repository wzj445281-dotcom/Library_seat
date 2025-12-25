const app = getApp()
import userApi from '../../api/user'

Page({
    data: {
        userInfo: null,
        hasUserInfo: false,
        reservations: [] // 预约列表
    },

    onShow() {
        this.checkLogin()
    },

    checkLogin() {
        const userInfo = wx.getStorageSync('userInfo')
        if (userInfo) {
            this.setData({
                userInfo,
                hasUserInfo: true
            })
            this.fetchReservations()
        } else {
            this.setData({ hasUserInfo: false, reservations: [] })
        }
    },

    toLogin() {
        wx.navigateTo({ url: '/pages/login/login' })
    },

    /**
     * 获取预约列表
     */
    async fetchReservations() {
        try {
            const res = await userApi.getMyReservations()
            if (res.code === 200) {
                // 简单格式化一下时间展示
                const list = res.data.map(item => {
                    // 假设 item.startTime 是 "2025-12-25T09:30:00"
                    item.displayTime = item.startTime.replace('T', ' ').substring(0, 16)
                    return item
                })
                this.setData({ reservations: list })
            }
        } catch (err) {
            console.error(err)
        }
    },

    // 退出登录
    logout() {
        wx.clearStorageSync()
        this.setData({ userInfo: null, hasUserInfo: false, reservations: [] })
    }
})