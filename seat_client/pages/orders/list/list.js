// pages/orders/list.js
const app = getApp();
// 注意路径：根据文件位置 ../../api/order.js
const orderApi = require('../../api/order.js');

Page({
  data: {
    currentTab: 0, // 0: 当前订单, 1: 历史订单
    loading: true,
    list: []
  },

  onShow() {
    // 检查登录状态
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showModal({
        title: '提示',
        content: '请先登录后查看订单',
        showCancel: true,
        confirmText: '去登录',
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
    // Tab 0 (当前): 筛选 MAKING, WAIT_PICKUP, PENDING
    // Tab 1 (历史): 筛选 FINISHED, CANCELLED
    const statusType = this.data.currentTab === 0 ? 'current' : 'history';

    orderApi.getOrderList(statusType)
        .then(res => {
          // 兼容处理：如果 res.data 是数组则直接用，如果是 Page 对象则取 records
          const rawList = Array.isArray(res.data) ? res.data : (res.data?.records || []);

          // 数据清洗与映射
          const list = rawList.map(item => this.mapOrderItem(item));

          this.setData({ list: list });
        })
        .catch(err => {
          console.error('获取订单列表失败', err);
          wx.showToast({ title: '加载失败', icon: 'none' });
        })
        .finally(() => {
          this.setData({ loading: false });
          if (cb) cb();
        });
  },

  /**
   * 将后端数据映射为前端 ViewModel
   */
  mapOrderItem(item) {
    // 1. 状态转换
    let statusText = '';
    let statusClass = item.status; // 用于 CSS 类名

    switch (item.status) {
      case 'PENDING': statusText = '待支付'; break;
      case 'PAID': statusText = '已支付'; break;
      case 'MAKING': statusText = '制作中'; break;
      case 'READY': statusText = '待取餐'; break;
      case 'COMPLETED': statusText = '已完成'; break;
      case 'CANCELLED': statusText = '已取消'; break;
      case 'REFUNDED': statusText = '已退款'; break;
      default: statusText = item.status;
    }

    // 2. 商品描述 (例如：拿铁 等2件)
    let desc = '';
    let totalCount = 0;
    if (item.items && item.items.length > 0) {
      desc = item.items[0].productName;
      totalCount = item.items.reduce((sum, it) => sum + it.quantity, 0);
      if (totalCount > 1) {
        desc += ` 等${totalCount}件`;
      }
    } else {
      desc = '瑞幸咖啡'; // 兜底
    }

    // 3. 时间格式化 (简单处理 T)
    let time = item.createTime ? item.createTime.replace('T', ' ') : '';

    return {
      id: item.id,
      orderNo: item.orderNo,
      shopName: '瑞幸咖啡 (默认门店)',
      status: item.status,
      statusClass: statusClass,
      statusText: statusText,
      time: time,
      totalPrice: item.totalAmount,
      totalCount: totalCount,
      desc: desc
    };
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    // 这里的 id 应该是 orderNo 或者数据库 ID，取决于详情页需要什么
    // 假设详情页接收 orderNo
    const orderNo = this.data.list.find(i => i.id === id)?.orderNo;

    wx.navigateTo({
      url: `/pages/orders/detail/detail?orderNo=${orderNo || id}`
    });
  },

  reOrder(e) {
    // "再来一单" -> 跳转菜单
    wx.switchTab({
      url: '/pages/menu/index'
    });
  },

  // 去支付
  goPay(e) {
    const id = e.currentTarget.dataset.id;
    // 实际开发中跳转收银台或调起支付，这里模拟跳转详情
    const orderNo = this.data.list.find(i => i.id === id)?.orderNo;
    wx.navigateTo({
      url: `/pages/orders/detail/detail?orderNo=${orderNo || id}`
    });
  }
});