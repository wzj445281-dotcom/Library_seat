const app = getApp(); 
const { request } = require('../../utils/request'); 

Page({ 
  data: { 
    currentTab: 0, // 0: 领券中心, 1: 我的卡包 
    availableCoupons: [], // 可领列表 
    myCoupons: [],        // 已领列表 
    loading: true 
  }, 

  onShow() { 
    this.loadData(); 
  }, 

  // 切换 Tab 
  switchTab(e) { 
    const idx = e.currentTarget.dataset.idx; 
    this.setData({ currentTab: idx }); 
    this.loadData(); 
  }, 

  loadData() { 
    this.setData({ loading: true }); 
    if (this.data.currentTab === 0) { 
      this.fetchAvailableCoupons(); 
    } else { 
      this.fetchMyCoupons(); 
    } 
  }, 

  // 1. 获取可领取的优惠券 
  fetchAvailableCoupons() { 
    request.get('/api/app/coupon/list').then(res => { 
      if (res.code === 200) { 
        this.setData({ 
          availableCoupons: res.data, 
          loading: false 
        }); 
      } 
    }); 
  }, 

  // 2. 获取我的优惠券 
  fetchMyCoupons() { 
    request.get('/api/app/coupon/my').then(res => { 
      if (res.code === 200) { 
        this.setData({ 
          myCoupons: res.data, 
          loading: false 
        }); 
      } 
    }); 
  }, 

  // 3. 核心动作：立即领取 
  onAcquire(e) { 
    // 震动反馈 
    wx.vibrateShort({ type: 'light' }); 

    const { id, index } = e.currentTarget.dataset; 
    
    wx.showLoading({ title: '抢券中...' }); 

    request.post(`/api/app/coupon/acquire/${id}`).then(res => { 
      wx.hideLoading(); 
      if (res.code === 200) { 
        wx.showToast({ title: '领取成功', icon: 'success' }); 
        // 简单粗暴：领取成功后，刷新列表更新库存显示 
        this.fetchAvailableCoupons(); 
      } else { 
        wx.showToast({ title: res.msg || '领取失败', icon: 'none' }); 
      } 
    }).catch(() => { 
      wx.hideLoading(); 
    }); 
  } 
});