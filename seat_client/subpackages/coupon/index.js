// subpackages/coupon/index.js
const couponApi = require('../../api/coupon.js');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    tabs: ['我的优惠券', '领券中心'],
    activeTab: 0,
    myCoupons: [], // 我的优惠券列表
    availableCoupons: [], // 可领取优惠券列表
    loading: false
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.loadData();
  },

  /**
   * 切换 Tab
   */
  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ activeTab: index });
    this.loadData();
  },

  /**
   * 加载数据
   */
  loadData() {
    if (this.data.activeTab === 0) {
      this.fetchMyCoupons();
    } else {
      this.fetchAvailableCoupons();
    }
  },

  /**
   * 获取我的优惠券
   */
  fetchMyCoupons() {
    this.setData({ loading: true });
    couponApi.getMyCoupons()
        .then(res => {
          // 假设后端返回的数据结构中 data 是列表
          // 如果没有过期时间，可以根据业务逻辑处理
          const list = (res.data || []).map(item => {
            // 简单的状态格式化
            item.statusText = item.status === 0 ? '未使用' : (item.status === 1 ? '已使用' : '已过期');
            return item;
          });
          this.setData({ myCoupons: list, loading: false });
        })
        .catch(err => {
          console.error('获取我的优惠券失败', err);
          this.setData({ loading: false });
        });
  },

  /**
   * 获取领券中心列表
   */
  fetchAvailableCoupons() {
    this.setData({ loading: true });
    couponApi.getCouponList()
        .then(res => {
          this.setData({ availableCoupons: res.data || [], loading: false });
        })
        .catch(err => {
          console.error('获取领券中心数据失败', err);
          this.setData({ loading: false });
        });
  },

  /**
   * 领取优惠券
   */
  handleReceive(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;

    wx.showLoading({ title: '领取中...' });
    couponApi.receiveCoupon(id)
        .then(res => {
          wx.hideLoading();
          wx.showToast({ title: '领取成功', icon: 'success' });
          // 刷新列表更新库存或状态
          this.fetchAvailableCoupons();
        })
        .catch(err => {
          wx.hideLoading();
          wx.showToast({ title: err.msg || '领取失败', icon: 'none' });
        });
  },

  /**
   * 去使用（跳转到点餐页）
   */
  goUse() {
    wx.switchTab({
      url: '/pages/menu/index'
    });
  },

  onPullDownRefresh() {
    this.loadData();
    wx.stopPullDownRefresh();
  }
})