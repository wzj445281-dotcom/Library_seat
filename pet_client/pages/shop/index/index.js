const app = getApp()
import productApi from '../../../api/product'

Page({
    data: {
        categories: ['全部', '主粮', '零食', '玩具', '医疗', '周边'],
        currentCategoryIndex: 0,
        productList: [],
        loading: false,
        page: 1,
        hasMore: true
    },

    onLoad(options) {
        this.fetchProducts(true)
    },

    onPullDownRefresh() {
        this.setData({ page: 1, hasMore: true })
        this.fetchProducts(true).then(() => {
            wx.stopPullDownRefresh()
        })
    },

    onReachBottom() {
        if (this.data.hasMore && !this.data.loading) {
            this.setData({ page: this.data.page + 1 })
            this.fetchProducts(false)
        }
    },

    /**
     * 切换分类
     */
    onCategoryTap(e) {
        const index = e.currentTarget.dataset.index
        if (index === this.data.currentCategoryIndex) return

        this.setData({
            currentCategoryIndex: index,
            page: 1,
            hasMore: true,
            productList: [] // 切换分类清空列表
        })
        this.fetchProducts(true)
    },

    /**
     * 拉取商品数据
     */
    async fetchProducts(refresh) {
        this.setData({ loading: true })
        try {
            const category = this.data.categories[this.data.currentCategoryIndex]
            const query = {
                current: this.data.page,
                size: 10
            }
            // 如果不是"全部"，则添加分类参数
            if (category !== '全部') {
                query.category = category
            }

            const res = await productApi.getProductList(query)
            if (res.code === 200) {
                // 兼容后端返回 Page 结构或 List 结构
                const newItems = res.data.records || res.data || []

                // 如果返回数据少于请求条数，说明没有更多了
                if (newItems.length < query.size) {
                    this.setData({ hasMore: false })
                }

                this.setData({
                    productList: refresh ? newItems : [...this.data.productList, ...newItems]
                })
            }
        } catch (err) {
            console.error(err)
            wx.showToast({ title: '加载失败', icon: 'none' })
        } finally {
            this.setData({ loading: false })
        }
    },

    /**
     * 跳转详情
     */
    toDetail(e) {
        const id = e.currentTarget.dataset.id
        wx.navigateTo({
            url: `/pages/shop/detail/detail?id=${id}`
        })
    }
})