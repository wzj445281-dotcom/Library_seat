// pages/orders/list.js
const app = getApp();
// 引入订单 API
const { getMyOrders, cancelOrder, confirmOrder } = require('../../api/order.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    currentTab: 0, // 0:全部, 1:待付款, 2:待发货, 3:待收货, 4:已完成
    orderList: [],
    loading: true,
    isLogin: false
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    const token = wx.getStorageSync('token');
    if (!token) {
      this.setData({ isLogin: false, orderList: [], loading: false });
      wx.showModal({
        title: '提示',
        content: '请先登录查看订单',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' });
          } else {
            wx.switchTab({ url: '/pages/index/index' });
          }
        }
      });
      return;
    }

    this.setData({ isLogin: true });
    this.loadOrderList();
  },

  /**
   * 切换 Tab
   */
  switchTab: function(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    if (this.data.currentTab === index) return;

    this.setData({
      currentTab: index,
      orderList: [], // 切换时先清空，优化体验
      loading: true
    });
    this.loadOrderList();
  },

  /**
   * 加载订单列表 - 真实 API 调用
   */
  loadOrderList: function() {
    if (!this.data.isLogin) return;

    // 映射前端 Tab 索引到后端状态码
    // 假设后端: 0-待付款, 1-待发货, 2-待收货, 3-已完成, 4-已取消
    const statusMap = {
      0: null, // 全部
      1: 0,    // 待付款
      2: 1,    // 待发货
      3: 2,    // 待收货
      4: 3     // 已完成
    };

    const status = statusMap[this.data.currentTab];

    getMyOrders({ status: status }).then(res => {
      if (res.code === 200) {
        this.setData({
          orderList: res.data || [],
          loading: false
        });
      } else {
        wx.showToast({
          title: res.msg || '获取订单失败',
          icon: 'none'
        });
        this.setData({ loading: false });
      }
    }).catch(err => {
      console.error("API Error", err);
      this.setData({ loading: false });
      wx.showToast({
        title: '网络异常',
        icon: 'none'
      });
    });
  },

  /**
   * 取消订单
   */
  handleCancelOrder: function(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中' });
          cancelOrder(orderId).then(apiRes => {
            wx.hideLoading();
            if(apiRes.code === 200) {
              wx.showToast({ title: '取消成功' });
              this.loadOrderList(); // 刷新列表
            } else {
              wx.showToast({ title: apiRes.msg || '操作失败', icon: 'none' });
            }
          });
        }
      }
    });
  },

  /**
   * 确认收货
   */
  handleConfirmOrder: function(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认已收到商品？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中' });
          confirmOrder(orderId).then(apiRes => {
            wx.hideLoading();
            if(apiRes.code === 200) {
              wx.showToast({ title: '收货成功' });
              this.loadOrderList(); // 刷新列表
            } else {
              wx.showToast({ title: apiRes.msg || '操作失败', icon: 'none' });
            }
          });
        }
      }
    });
  }
})