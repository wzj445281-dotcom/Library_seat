const app = getApp()
import doctorApi from '../../../api/doctor'

Page({
  data: {
    doctorList: [],
    loading: true
  },

  onLoad() {
    this.fetchDoctors()
  },

  /**
   * 获取医生数据
   */
  async fetchDoctors() {
    this.setData({ loading: true })
    try {
      const res = await doctorApi.getDoctorList()
      if (res.code === 200) {
        // 给图片添加前缀（如果是相对路径），或者处理默认图
        const list = res.data.map(item => {
          if (!item.avatar || item.avatar.trim() === '') {
            item.avatar = 'https://via.placeholder.com/150' // 默认头像
          }
          return item
        })
        this.setData({ doctorList: list })
      }
    } catch (err) {
      console.error('获取医生列表失败', err)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  /**
   * 跳转到AI咨询 (带入医生上下文)
   */
  toConsult(e) {
    const doctorName = e.currentTarget.dataset.name
    wx.navigateTo({
      url: `/pages/ai_chat/ai_chat?context=consult&doctor=${doctorName}`
    })
  },

  /**
   * 预约 (暂时打印日志，后续开发预约页)
   */
  toBook(e) {
    const doctorId = e.currentTarget.dataset.id
    // 获取当前点击的医生完整信息，传递给下一个页面
    const doctor = this.data.doctorList.find(item => item.id === doctorId)

    // 携带参数跳转
    wx.navigateTo({
      url: `/pages/doctor/booking/booking?id=${doctorId}&name=${doctor.name}&title=${doctor.title}&avatar=${encodeURIComponent(doctor.avatar)}`
    })
  },
})