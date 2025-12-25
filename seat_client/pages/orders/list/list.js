const OrderAPI = require('../../../api/order.js');

Page({
  data: {
    tabs: ['全部', '制作中', '待取餐', '历史订单'],
    activeTab: 0,
    orders: [],
    loading: false,
    page: 1,
    hasMore: true
  },

  onLoad: function (options) {
    this.loadOrders(true);
  },

  onShow: function () {
    this.loadOrders(true);
  },

  onPullDownRefresh: function () {
    this.loadOrders(true);
  },

  onReachBottom: function () {
    if (this.data.hasMore && !this.data.loading) {
      this.loadOrders(false);
    }
  },

  onTabClick: function (e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ activeTab: index });
    this.loadOrders(true);
  },

  loadOrders: function (refresh = false) {
    if (this.data.loading) return;
    
    this.setData({ loading: true });
    const page = refresh ? 1 : this.data.page;

    // 根据当前选中的标签确定状态参数
    let status = '';
    switch (this.data.activeTab) {
      case 1:
        status = 'PAID';
        break;
      case 2:
        status = 'READY';
        break;
      case 3:
        status = 'COMPLETED';
        break;
      default:
        status = '';
    }

    OrderAPI.getMyOrders(status).then(res => {
      const newOrders = (res || []).map(order => this.formatOrder(order));
      
      this.setData({
        orders: refresh ? newOrders : this.data.orders.concat(newOrders),
        page: page + 1,
        hasMore: newOrders.length === 10,
        loading: false
      });
      
      if (refresh) wx.stopPullDownRefresh();
    }).catch(err => {
      console.error(err);
      this.setData({ loading: false });
      if (refresh) wx.stopPullDownRefresh();
    });
  },

  formatOrder: function (order) {
    let statusText = '';
    let statusColorClass = ''; 
    let isReady = false; 

    // 状态映射 - 使用后端返回的状态字符串
    switch (order.status) {
      case 'PENDING':
        statusText = '待支付';
        statusColorClass = 'text-orange';
        break;
      case 'PAID':
        statusText = '制作中';
        statusColorClass = 'text-orange';
        break;
      case 'READY':
        statusText = '待取餐'; 
        statusColorClass = 'text-luckin'; 
        isReady = true; 
        break;
      case 'COMPLETED':
        statusText = '已完成';
        statusColorClass = 'text-green';
        break;
      case 'CANCELLED':
        statusText = '已取消';
        statusColorClass = 'text-gray';
        break;
      default:
        statusText = '未知状态';
        statusColorClass = 'text-gray';
    }

    let totalCount = 0;
    let formattedItems = [];

    if (order.items) {
      order.items.forEach(item => {
        totalCount += item.quantity;
        formattedItems.push({
          ...item,
          // 如果没有图片，使用默认咖啡占位图
          // 实际项目中应从 item.productImage 获取
          image: item.productImage || '/assets/images/book-default.png',
          // 简化过长的商品名
          shortName: item.productName.length > 5 ? item.productName.substring(0, 5) + '...' : item.productName
        });
      });
    }

    const pickupCode = order.pickupCode || (order.id ? order.id.toString().slice(-3) : '888');

    return {
      ...order,
      statusText,
      statusColorClass,
      isReady,
      pickupCode,
      totalCount,
      // 将处理好的商品列表传给前端
      items: formattedItems,
      createTimeFormatted: order.createTime ? order.createTime.replace('T', ' ') : ''
    };
  },

  onOrderAgain: function (e) {
    wx.switchTab({
      url: '/pages/menu/index'
    });
  },

  onViewDetail: function (e) {
    const orderId = e.currentTarget.dataset.id;
    console.log("查看详情", orderId);
  }
});