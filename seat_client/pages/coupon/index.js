const request = require('../../utils/request.js');

Page({
  data: {
    status: 0, // 0:未使用, 1:已使用, 2:已过期
    loading: true,
    isRefreshing: false,
    couponList: []
  },

  onLoad() {
    this.fetchCoupons();
  },

  // 切换 Tab
  switchTab(e) {
    const status = parseInt(e.currentTarget.dataset.status);
    if (status === this.data.status) return;

    this.setData({ status, couponList: [], loading: true });
    this.fetchCoupons();
  },

  // 下拉刷新
  onRefresh() {
    this.setData({ isRefreshing: true });
    this.fetchCoupons();
  },

  // 获取数据
  async fetchCoupons() {
    try {
      // 模拟请求，替换为您的真实接口 request.get('/app/coupon/my', { status: this.data.status })
      // const res = await request.get('/app/coupon/my', { status: this.data.status });

      // 这里使用 Mock 数据演示效果
      await new Promise(resolve => setTimeout(resolve, 500));
      const mockData = this.getMockData(this.data.status);

      this.setData({
        couponList: mockData,
        loading: false,
        isRefreshing: false
      });

    } catch (err) {
      console.error(err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false, isRefreshing: false });
    }
  },

  goUse() {
    wx.switchTab({ url: '/pages/menu/index' });
  },

  // 生成模拟数据
  getMockData(status) {
    if (status === 0) {
      return [
        { id: 1, amount: 5, title: '全场通用立减券', minPoint: 20, startTime: '2023.12.01', endTime: '2025.12.31' },
        { id: 2, amount: 12, title: '新人拿铁专享券', minPoint: 25, startTime: '2023.12.10', endTime: '2026.01.15' },
        { id: 3, amount: 3, title: '免运费抵扣券', minPoint: 0, startTime: '2023.11.01', endTime: '2025.12.30' }
      ];
    } else if (status === 1) {
      return [
        { id: 4, amount: 8, title: '美式咖啡特价券', minPoint: 15, startTime: '2023.10.01', endTime: '2023.10.31' }
      ];
    } else {
      return [
        { id: 5, amount: 2, title: '限时优惠券', minPoint: 10, startTime: '2023.01.01', endTime: '2023.01.03' }
      ];
    }
  }
});
