const app = getApp();
const { getMyOrders } = require('../../api/order.js');

Page({
  data: {
    orders: [],
    loading: false
  },

  onLoad: function () {
    this.loadOrders();
  },

  onShow: function () {
    // 每次显示页面时刷新订单列表
    this.loadOrders();
  },

  // 加载订单列表
  loadOrders() {
    this.setData({ loading: true });

    getMyOrders().then(res => {
      this.setData({ 
        orders: res.data || [],
        loading: false 
      });
    }).catch(err => {
      console.error('加载订单失败:', err);
      this.setData({ loading: false });
      
      // 模拟数据
      const mockOrders = [
        {
          id: 1,
          orderNo: 'ORD20231225001',
          status: 0, // 0-待支付 1-待发货 2-待收货 3-已完成
          totalAmount: 199.00,
          createTime: '2023-12-25 10:30:00',
          items: [
            { name: '全价猫粮 10kg', count: 1, price: 199.00 }
          ]
        },
        {
          id: 2,
          orderNo: 'ORD20231225002',
          status: 2,
          totalAmount: 89.00,
          createTime: '2023-12-25 09:15:00',
          items: [
            { name: '宠物自动饮水机', count: 1, price: 89.00 }
          ]
        }
      ];
      
      this.setData({ orders: mockOrders });
    });
  },

  // 取消订单
  cancelOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '提示',
      content: '确定要取消此订单吗？',
      success: (res) => {
        if (res.confirm) {
          // 这里应该调用取消订单的API
          wx.showToast({ title: '订单已取消', icon: 'success' });
          this.loadOrders();
        }
      }
    });
  },

  // 确认收货
  confirmOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '提示',
      content: '确认已收到商品吗？',
      success: (res) => {
        if (res.confirm) {
          // 这里应该调用确认收货的API
          wx.showToast({ title: '已确认收货', icon: 'success' });
          this.loadOrders();
        }
      }
    });
  },

  // 跳转到订单详情
  goToDetail(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/orders/detail?id=${orderId}`
    });
  },

  // 获取订单状态文本
  getStatusText(status) {
    const statusMap = {
      0: '待支付',
      1: '待发货',
      2: '待收货',
      3: '已完成',
      4: '已取消'
    };
    return statusMap[status] || '未知状态';
  }
});