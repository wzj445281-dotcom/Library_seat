const request = require('../../utils/request.js');

Page({
  data: {
    currentTab: 0, // 0:全部, 1:待支付, 2:制作中, 3:待取货
    orderList: [],
    page: 1,
    hasMore: true
  },

  onShow() {
    // 检查登录状态
    const token = wx.getStorageSync('token');
    if (!token) {
      // 未登录，显示提示
      this.setData({ 
        page: 1, 
        orderList: [],
        currentTab: 0
      });
      wx.showModal({
        title: '提示',
        content: '查看订单需要登录，是否去登录？',
        confirmText: '去登录',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/login/login'
            });
          } else {
            // 取消后返回上一页
            wx.navigateBack();
          }
        }
      });
      return;
    }
    
    // 每次显示页面刷新数据
    this.setData({ page: 1, orderList: [] });
    this.fetchOrders();
  },

  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({
      currentTab: index,
      page: 1,
      orderList: [],
      hasMore: true
    });
    this.fetchOrders();
  },

  fetchOrders() {
    if (!this.data.hasMore) return;

    // 映射 Tab 到后端状态字段
    let status = '';
    switch(this.data.currentTab) {
      case 1: status = 'PENDING_PAY'; break;
      case 2: status = 'PAID'; break;
      case 3: status = 'READY'; break;
      default: status = ''; // 全部
    }

    wx.showLoading({ title: '加载中' });
    
    // 调用后端接口
    request.get('/app/store/order/list', { status }).then(res => {
      wx.hideLoading();
      if (res.code === 200) {
        const list = res.data.map(item => this.processItem(item));
        
        this.setData({
          orderList: this.data.page === 1 ? list : this.data.orderList.concat(list),
          hasMore: list.length >= 10, // 假设每页10条
          page: this.data.page + 1
        });
      } else {
        wx.showToast({ title: res.message || '加载失败', icon: 'none' });
      }
    }).catch(err => {
      wx.hideLoading();
      wx.showToast({ title: '网络异常', icon: 'none' });
      console.error('获取订单列表失败:', err);
    });
  },

  // 处理订单显示状态
  processItem(item) {
    let statusText = '';
    let statusStyle = '';
    
    switch(item.status) {
      case 'PENDING_PAY': 
        statusText = '待支付'; statusStyle = 'pending'; break;
      case 'PAID': 
        statusText = '制作中'; statusStyle = 'processing'; break;
      case 'READY': 
        statusText = item.type === 1 ? '待自取' : '配送中'; statusStyle = 'ready'; break;
      case 'COMPLETED': 
        statusText = '已完成'; statusStyle = 'completed'; break;
      case 'CANCELLED': 
        statusText = '已取消'; statusStyle = 'completed'; break;
    }
    return { ...item, statusText, statusStyle };
  },

  // 模拟支付
  payOrder(e) {
    const orderNo = e.currentTarget.dataset.id;
    wx.showModal({
      title: '支付确认',
      content: '模拟支付该订单？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '支付中...' });
          // 调用后端支付接口
          request.post('/app/store/order/pay', { orderNo }).then(res => {
            wx.hideLoading();
            if (res.code === 200) {
              wx.showToast({ title: '支付成功' });
              this.onShow(); // 刷新列表
            } else {
              wx.showToast({ title: res.message || '支付失败', icon: 'none' });
            }
          }).catch(err => {
            wx.hideLoading();
            wx.showToast({ title: '支付异常', icon: 'none' });
            console.error('支付错误:', err);
          });
        }
      }
    });
  },

  // 取消订单
  cancelOrder(e) {
    const orderNo = e.currentTarget.dataset.id;
    wx.showModal({
      title: '取消确认',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '取消中...' });
          // 调用后端取消订单接口
          request.post('/app/store/order/cancel', { orderNo }).then(res => {
            wx.hideLoading();
            if (res.code === 200) {
              wx.showToast({ title: '订单已取消' });
              this.onShow(); // 刷新列表
            } else {
              wx.showToast({ title: res.message || '取消失败', icon: 'none' });
            }
          }).catch(err => {
            wx.hideLoading();
            wx.showToast({ title: '取消异常', icon: 'none' });
            console.error('取消订单错误:', err);
          });
        }
      }
    });
  },

  goToDetail(e) {
    const orderNo = e.currentTarget.dataset.id;
    if (orderNo) {
      wx.navigateTo({ 
        url: `/pages/orders/detail/detail?orderNo=${orderNo}` 
      });
    }
  },

  // Mock 数据生成 (如果后端接口未就绪可用此测试)
  mockFetch(status) {
    return new Promise(resolve => {
      setTimeout(() => {
        const mockList = [
          {
            id: 'ORD_1001',
            status: 'PENDING_PAY',
            totalAmount: '59.90',
            totalCount: 1,
            type: 1,
            createTime: '2023-10-25 10:00',
            products: [{ imgUrl: '', name: 'Java书' }]
          },
          {
            id: 'ORD_1002',
            status: 'READY',
            totalAmount: '32.00',
            totalCount: 2,
            type: 1,
            pickupCode: 'A808',
            createTime: '2023-10-24 14:30',
            products: [{ imgUrl: '' }, { imgUrl: '' }]
          }
        ];
        
        // 简单过滤
        const filtered = status ? mockList.filter(i => i.status === status) : mockList;
        resolve({ list: filtered });
      }, 500);
    });
  }
});