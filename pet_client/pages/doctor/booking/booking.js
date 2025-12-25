const app = getApp()
import doctorApi from '../../../api/doctor'

Page({
    data: {
        doctorId: null,
        doctorInfo: {}, // 暂存医生基本信息
        dates: [],      // 未来7天日期数组
        selectedDateIndex: 0, // 当前选中的日期下标
        currentDateStr: '',   // 当前选中的日期 YYYY-MM-DD

        timeSlots: [],  // 后端返回的时间槽
        selectedSlot: null, // 用户选中的时间槽 (例如 "09:30")

        submitting: false
    },

    onLoad(options) {
        if (options.id) {
            this.setData({ doctorId: options.id })
            // 如果页面跳转带了名字和头像，直接用，减少加载感
            if (options.name) {
                this.setData({
                    'doctorInfo.name': options.name,
                    'doctorInfo.avatar': options.avatar || 'https://via.placeholder.com/150',
                    'doctorInfo.title': options.title || '主治医师'
                })
            }
        }

        this.initCalendar()
        this.fetchSchedule()
    },

    /**
     * 初始化未来7天日历
     */
    initCalendar() {
        const dates = []
        const today = new Date()
        const weekMap = ['日', '一', '二', '三', '四', '五', '六']

        for (let i = 0; i < 7; i++) {
            const d = new Date(today)
            d.setDate(today.getDate() + i)

            const year = d.getFullYear()
            const month = (d.getMonth() + 1).toString().padStart(2, '0')
            const day = d.getDate().toString().padStart(2, '0')
            const dateStr = `${year}-${month}-${day}`

            dates.push({
                fullDate: dateStr,
                dayNum: day,
                weekStr: i === 0 ? '今天' : '周' + weekMap[d.getDay()]
            })
        }

        this.setData({
            dates,
            currentDateStr: dates[0].fullDate
        })
    },

    /**
     * 切换日期
     */
    onDateSelect(e) {
        const index = e.currentTarget.dataset.index
        const dateStr = this.data.dates[index].fullDate

        this.setData({
            selectedDateIndex: index,
            currentDateStr: dateStr,
            selectedSlot: null // 切换日期后清空选中的时间
        })

        this.fetchSchedule()
    },

    /**
     * 获取排班数据
     */
    async fetchSchedule() {
        if (!this.data.doctorId) return

        wx.showLoading({ title: '加载排班...' })
        try {
            const res = await doctorApi.getSchedule(this.data.doctorId, this.data.currentDateStr)
            if (res.code === 200) {
                this.setData({ timeSlots: res.data })
            } else {
                this.setData({ timeSlots: [] })
            }
        } catch (err) {
            console.error(err)
            wx.showToast({ title: '排班加载失败', icon: 'none' })
        } finally {
            wx.hideLoading()
        }
    },

    /**
     * 选择时间槽
     */
    onSlotSelect(e) {
        const slot = e.currentTarget.dataset.slot
        this.setData({ selectedSlot: slot })
    },

    /**
     * 提交预约
     */
    async submitBooking() {
        if (!this.data.selectedSlot) {
            wx.showToast({ title: '请选择就诊时间', icon: 'none' })
            return
        }

        this.setData({ submitting: true })

        // 构造完整的 ISO 时间字符串: "2025-12-25 09:30:00"
        const startTime = `${this.data.currentDateStr} ${this.data.selectedSlot}:00`

        try {
            const res = await doctorApi.createReservation({
                doctorId: this.data.doctorId,
                startTime: startTime,
                note: '用户小程序端预约'
            })

            if (res.code === 200) {
                wx.showToast({ title: '预约成功', icon: 'success' })
                setTimeout(() => {
                    // 跳转到订单/预约列表页
                    wx.switchTab({ url: '/pages/mine/mine' })
                }, 1500)
            } else {
                wx.showToast({ title: res.message || '预约失败', icon: 'none' })
            }
        } catch (err) {
            console.error(err)
            wx.showToast({ title: '网络异常', icon: 'none' })
        } finally {
            this.setData({ submitting: false })
        }
    }
})