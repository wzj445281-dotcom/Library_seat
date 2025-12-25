const app = getApp()
import productApi from '../../../api/product'
import orderApi from '../../../api/order' // 需要确保这个文件存在

Page({
    data: {
        id: null,
        product: {},
        loading: true,
        submitting: false
    },

    onLoad(options) {
        if (options.id) {
            this.setData({ id: options.id })
            this.fetchDetail(options.id)
        }
    },

    async fetchDetail(id) {
        this.setData({ loading: true })
        try {
            const res = await productApi.getProductDetail(id)
            if (res.code === 200) {
                this.setData({ product: res.data })
            } else {
                wx.showToast({ title: '商品不存在', icon: 'none' })
                setTimeout(() => wx.navigateBack(), 1500)
            }
        } catch (err) {
            console.error(err)
            wx.showToast({ title: '网络错误', icon: 'none' })
        } finally {
            this.setData({ loading: false })
        }
    },

    /**
     * 立即购买
     */
    async handleBuy() {
        // 检查登录
        const userInfo = wx.getStorageSync('userInfo')
        if (!userInfo) {
            wx.navigateTo({ url: '/pages/login/login' })
            return
        }

        // 简单模拟下单确认
        wx.showModal({
            title: '确认购买',
            content: `确定要花费 ¥${this.data.product.price} 购买该商品吗？`,
            success: async (res) => {
                if (res.confirm) {
                    this.createOrder()
                }
            }
        })
    },

    /**
     * 创建订单
     */
    async createOrder() {
        this.setData({ submitting: true })
        try {
            // 调用下单 API
            const res = await orderApi.createOrder({
                productId: this.data.product.id,
                count: 1, // 默认买1个，后续可加步进器
                amount: this.data.product.price
            })

            if (res.code === 200) {
                wx.showToast({ title: '下单成功', icon: 'success' })
                setTimeout(() => {
                    // 跳转到订单列表页 (需要确保该页面存在)
                    wx.navigateTo({ url: '/pages/orders/list' })
                }, 1500)
            } else {
                wx.showToast({ title: res.message || '下单失败', icon: 'none' })
            }
        } catch (err) {
            console.error(err)
            wx.showToast({ title: '服务暂不可用', icon: 'none' })
        } finally {
            this.setData({ submitting: false })
        }
    },

    // 联系客服
    contactService() {
        wx.showToast({ title: '客服正如火如荼赶来...', icon: 'none' })
    }
})