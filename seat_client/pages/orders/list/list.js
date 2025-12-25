const orderApi = require('../../api/order.js')
const app = getApp()

Page({
    data: {
        tabs: ['全部', '待取餐', '已完成'],
        activeTab: 0,
        orders: [],
        loading: false
    },

    onShow() {
        this.loadOrders()
    },

    // 切换 Tab
    onTabChange(e) {
        const index = e.currentTarget.dataset.index
        this.setData({ activeTab: index, orders: [] })
        this.loadOrders()
    },

    // 加载订单
    async loadOrders() {
        this.setData({ loading: true })
        try {
            // 简单的状态映射：0=全部, 1=READY(待取), 2=COMPLETED(已完成)
            // 注意：这里为了简化，Tab 1 只查 READY。实际业务可能需要查 PAID + READY
            let status = ''
            if (this.data.activeTab === 1) status = 'READY'
            if (this.data.activeTab === 2) status = 'COMPLETED'

            const res = await orderApi.getMyOrders(status)

            // 格式化数据 (处理时间、状态显示)
            const list = (res.data || []).map(item => {
                item.statusText = this.getStatusText(item.status)
                item.totalCount = item.products ? item.products.reduce((sum, p) => sum + p.quantity, 0) : 0
                return item
            })

            this.setData({ orders: list })
        } catch (err) {
            console.error(err)
            wx.showToast({ title: '加载失败', icon: 'none' })
        } finally {
            this.setData({ loading: false })
        }
    },

    // 辅助：状态文案
    getStatusText(status) {
        const map = {
            'PENDING': '待付款',
            'PAID': '制作中', // 瑞幸模式：支付完就是制作中
            'READY': '待取餐',
            'COMPLETED': '已完成',
            'CANCELLED': '已取消'
        }
        return map[status] || status
    },

    // 取消订单
    async handleCancel(e) {
        const orderNo = e.currentTarget.dataset.no
        wx.showModal({
            title: '提示',
            content: '确定要取消订单吗？',
            success: async (res) => {
                if (res.confirm) {
                    try {
                        await orderApi.cancelOrder(orderNo)
                        wx.showToast({ title: '已取消' })
                        this.loadOrders() // 刷新列表
                    } catch (err) {
                        wx.showToast({ title: err.msg || '取消失败', icon: 'none' })
                    }
                }
            }
        })
    },

    // 去支付 (兜底逻辑，防止有漏网之鱼)
    async handlePay(e) {
        const orderNo = e.currentTarget.dataset.no
        try {
            await orderApi.payOrder(orderNo)
            wx.showToast({ title: '支付成功' })
            this.loadOrders()
        } catch (err) {
            wx.showToast({ title: '支付失败', icon: 'none' })
        }
    }
})