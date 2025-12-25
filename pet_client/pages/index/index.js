// pages/index/index.js
const app = getApp();
// 引入 API 模块 (确保这些文件在 api 目录下存在)
const { getProductList } = require('../../api/product.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    // 轮播图数据 (暂时写死，也可改为接口获取)
    bannerList: [
      { id: 1, url: 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80' },
      { id: 2, url: 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80' }
    ],
    // 分类导航
    categoryList: [
      { id: 1, name: '主粮', icon: '/assets/icons/food.png' }, // 确保图标资源存在，或者换成网络图片
      { id: 2, name: '零食', icon: '/assets/icons/snack.png' },
      { id: 3, name: '玩具', icon: '/assets/icons/toy.png' },
      { id: 4, name: '医疗', icon: '/assets/icons/medical.png' }
    ],
    productList: [],
    loading: true,
    refreshing: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    this.loadProducts();
  },

  /**
   * 加载商品数据 - 真实 API 调用
   */
  loadProducts: function() {
    // 如果不是下拉刷新，显示加载中
    if (!this.data.refreshing) {
      this.setData({ loading: true });
    }

    getProductList().then(res => {
      // 停止下拉刷新动画
      if(this.data.refreshing) {
        wx.stopPullDownRefresh();
        this.setData({ refreshing: false });
      }

      if (res.code === 200) {
        // 处理图片路径，如果后端返回的是相对路径，需要拼接域名
        const list = res.data.map(item => {
          // 这里假设后端直接返回了完整URL或者前端能处理
          return item;
        });

        this.setData({
          productList: list,
          loading: false
        });
      } else {
        wx.showToast({
          title: '加载商品失败',
          icon: 'none'
        });
        this.setData({ loading: false });
      }
    }).catch(err => {
      console.error("加载商品出错", err);
      this.setData({ loading: false });
      if(this.data.refreshing) {
        wx.stopPullDownRefresh();
        this.setData({ refreshing: false });
      }
      wx.showToast({
        title: '网络连接异常',
        icon: 'none'
      });
    });
  },

  /**
   * 跳转商品详情
   */
  goToDetail: function(e) {
    const id = e.currentTarget.dataset.id;
    // 确保 detail 页面存在
    wx.navigateTo({
      url: `/pages/product/detail?id=${id}`,
      fail: (err) => {
        console.error("跳转失败", err);
        wx.showToast({ title: '详情页开发中', icon: 'none' });
      }
    });
  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {
    this.setData({ refreshing: true });
    this.loadProducts();
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {
    return {
      title: '宠物商城',
      path: '/pages/index/index'
    }
  }
})