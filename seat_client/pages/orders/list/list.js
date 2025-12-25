const app = getApp();
const orderApi = require('../../../api/order.js');

Page({
  data: {
    currentTab: 0, // 0: 当前订单, 1: 历史订单
    loading: true,
    list: []
  },

  onShow() {
    // 每次进入页面刷新数据
    this.loadData();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadData(() => {
      wx.stopPullDownRefresh();
    });
  },

  switchTab(e) {
    const index = Number(e.currentTarget.dataset.index);
    if (index === this.data.currentTab) return;

    this.setData({
      currentTab: index,
      list: [], // 切换时先清空，防止视觉残留
      loading: true
    });

    this.loadData();
  },

  loadData(cb) {
    // 根据 Tab 映射状态
    // Tab 0 (当前): 筛选 MAKING, WAIT_PICKUP
    // Tab 1 (历史): 筛选 FINISHED, CANCELLED
    // 这里假设后端接收 'current' 和 'history' 关键词，或者由前端筛选
    // 为了简化，我们请求所有，前端过滤（如果数据量不大）
    // 商业级项目通常由后端支持分页和状态筛选 API

    const statusType = this.data.currentTab === 0 ? 'current' : 'history';

    orderApi.getOrderList(statusType)
        .then(res => {
          if (res.code === 200) {
            this.setData({ list: res.data || [] });
          }
        })
        .catch(err => {
          console.error('获取订单列表失败', err);
          // Mock 数据兜底，确保演示效果
          this.mockList(statusType);
        })
        .finally(() => {
          this.setData({ loading: false });
          if (cb) cb();
        });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/orders/detail/detail?id=${id}`
    });
  },

  reOrder(e) {
    // "再来一单"逻辑：通常是将该订单商品重新加入购物车并跳转菜单
    // 这里简化为直接跳转菜单
    wx.switchTab({
      url: '/pages/menu/index'
    });
  },

  // 兜底 Mock 数据
  mockList(type) {
    let mockData = [];
    if (type === 'current') {
      mockData = [
        {
          id: '1001',
          shopName: '瑞幸咖啡 (科技园店)',
          status: 'MAKING',
          statusText: '制作中',
          time: '2025-12-25 10:20',
          totalPrice: 42,
          totalCount: 2,
          desc: '生椰拿铁 等2件'
        }
      ];
    } else {
      mockData = [
        {
          id: '1002',
          shopName: '瑞幸咖啡 (科技园店)',
          status: 'FINISHED',
          statusText: '已完成',
          time: '2025-12-24 15:30',
          totalPrice: 21,
          totalCount: 1,
          desc: '美式咖啡 等1件'
        },
        {
          id: '1003',
          shopName: '瑞幸咖啡 (深大店)',
          status: 'CANCELLED',
          statusText: '已取消',
          time: '2025-12-23 09:10',
          totalPrice: 18,
          totalCount: 1,
          desc: '标准美式 等1件'
        }
      ];
    }

    // 模拟网络延迟
    setTimeout(() => {
      this.setData({ list: mockData });
    }, 500);
  }
});