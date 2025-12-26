const app = getApp();
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
            wx.switchTab({ url: '/pages/menu/index' });
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

  // 切换 Tab
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

  // 加载数据
  loadData(cb) {
    // Tab 0 (当前): 筛选 MAKING, WAIT_PICKUP, PENDING
    // Tab 1 (历史): 筛选 COMPLETED, CANCELLED
    const statusType = this.data.currentTab === 0 ? 'current' : 'history';

    this.setData({ loading: true });

    orderApi.getOrderList(statusType)
        .then(res => {
          // 兼容处理：如果 res.data 是数组则直接用
          const rawList = Array.isArray(res.data) ? res.data : (res.data?.records || []);

          // 数据清洗与映射
          const list = rawList.map(item => this.mapOrderItem(item));

          this.setData({ list: list, loading: false });
        })
        .catch(err => {
          console.error('获取订单列表失败', err);
          wx.showToast({ title: '加载失败', icon: 'none' });
          this.setData({ loading: false });
        })
        .finally(() => {
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
      case 'PAID': statusText = '制作中'; break; // 支付后即制作中
      case 'MAKING': statusText = '制作中'; break;
      case 'READY': statusText = '待取餐'; break;
      case 'WAIT_PICKUP': statusText = '待取餐'; break;
      case 'COMPLETED': statusText = '已完成'; break;
      case 'CANCELLED': statusText = '已取消'; break;
      case 'REFUNDED': statusText = '已退款'; break;
      default: statusText = item.status;
    }

    // 2. 商品描述 (例如：拿铁 等2件)
    let desc = '';
    let totalCount = 0;
    let firstImg = '/assets/images/logo.png'; // 默认图

    // 优先使用 products 字段
    const productList = item.products || item.items || [];

    if (productList && productList.length > 0) {
      const firstItem = productList[0];
      desc = firstItem.productName || firstItem.name;

      // 尝试获取图片，后端可能没返回全路径，这里简单处理
      // 实际上后端 OrderServiceImpl 已经填充了 products，包含 imgUrl
      // 但 OrderItem 实体里可能没有 imgUrl，需要看后端怎么填的
      // 这里如果前端没有图片，就用默认的

      totalCount = productList.reduce((sum, it) => sum + (it.quantity || it.count || 0), 0);
      if (totalCount > 1) {
        desc += ` 等${totalCount}件`;
      }
    } else {
      desc = '瑞幸咖啡';
    }

    return {
      id: item.id,
      orderNo: item.orderNo,
      shopName: '瑞幸咖啡 (默认门店)',
      status: item.status,
      statusClass: statusClass,
      statusText: statusText,
      time: item.createTime || '', // 后端已格式化
      totalPrice: item.totalAmount,
      totalCount: totalCount,
      desc: desc,
      // 如果需要显示商品图片，可以在这里处理
      productList: productList
    };
  },

  // 跳转详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    const orderNo = this.data.list.find(i => i.id === id)?.orderNo;
    // 使用 orderNo 跳转
    wx.navigateTo({
      url: `/pages/orders/detail/detail?orderNo=${orderNo}`
    });
  },

  // 再来一单
  reOrder(e) {
    wx.switchTab({
      url: '/pages/menu/index'
    });
  },

  // 去支付
  goPay(e) {
    const id = e.currentTarget.dataset.id;
    const orderNo = this.data.list.find(i => i.id === id)?.orderNo;
    wx.navigateTo({
      url: `/pages/orders/detail/detail?orderNo=${orderNo}`
    });
  }
});