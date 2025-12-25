const app = getApp()
import doctorApi from '../../api/doctor'
import productApi from '../../api/product'

Page({
  data: {
    banners: [
      { id: 1, img: 'https://via.placeholder.com/750x350/1296db/ffffff?text=PetSaaS+Opening' },
      { id: 2, img: 'https://via.placeholder.com/750x350/ff9800/ffffff?text=New+Year+Sale' }
    ],
    menuList: [
      { name: '预约挂号', icon: '/assets/icons/menu_doctor.png', url: '/pages/doctor/list/list', color: '#e6f7ff' },
      { name: 'AI问诊', icon: '/assets/icons/menu_ai.png', url: '/pages/ai_chat/ai_chat', color: '#fff7e6' },
      { name: '严选商城', icon: '/assets/icons/menu_shop.png', url: '/pages/shop/index/index', color: '#fff0f6' }, // 稍后开发商城页
      { name: '我的预约', icon: '/assets/icons/menu_cal.png', url: '/pages/mine/mine', color: '#f0f5ff' }
    ],
    recommendDoctors: [],
    hotProducts: [],
    loading: true
  },

  onLoad() {
    this.initData()
  },

  onPullDownRefresh() {
    this.initData().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  async initData() {
    this.setData({ loading: true })
    try {
      // 并行请求数据
      const [doctorRes, productRes] = await Promise.all([
        doctorApi.getDoctorList(),
        productApi.getRecommendProducts()
      ])

      if (doctorRes.code === 200) {
        // 只取前3名医生展示
        this.setData({ recommendDoctors: doctorRes.data.slice(0, 3) })
      }

      if (productRes.code === 200) {
        // 如果后端返回的是 Page 对象结构 (records)，则取 records，否则直接取 data
        const list = productRes.data.records || productRes.data || []
        this.setData({ hotProducts: list.slice(0, 4) })
      }

    } catch (err) {
      console.error('首页数据加载失败', err)
    } finally {
      this.setData({ loading: false })
    }
  },

  /**
   * 菜单跳转
   */
  onMenuTap(e) {
    const url = e.currentTarget.dataset.url
    if (url.includes('shop')) {
      wx.showToast({ title: '商城模块开发中', icon: 'none' })
      return
    }
    // 区分 switchTab 和 navigateTo
    if (url.includes('mine') || url.includes('ai_chat')) {
      wx.switchTab({ url })
    } else {
      wx.navigateTo({ url })
    }
  },

  /**
   * 跳转商品详情
   */
  toProductDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.showToast({ title: '即将前往商品详情', icon: 'none' })
    // 后续开发：wx.navigateTo({ url: `/pages/shop/detail/detail?id=${id}` })
  }
})