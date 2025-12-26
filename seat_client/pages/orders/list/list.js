const request = require('../../utils/request.js');

Page({
  data: {
    currentTab: 0, // 0:全部, 1:待支付, 2:制作中, 3:待取货
    orderList: [],
    page: 1,
    hasMore: true,
    loading: false
  },

  onShow() {
    this.checkLoginAndLoad();
  },

  checkLoginAndLoad() {
    const token = wx.getStorageSync('token');
    if (!token) {
      this.setData({ orderList: [] });
      wx.showModal({
        title: '提示',
        content: '查看订单需要登录，是否去登录？',
        success: (res) => {
          if (res.confirm) wx.navigateTo({ url: '/pages/login/login' });
          else wx.switchTab({ url: '/pages/menu/index' });
        }
      });
      return;
    }
    // 刷新数据
    this.refreshData();
  },

  refreshData() {
    this.setData({ page: 1, orderList: [], hasMore: true });
    this.fetchOrders();
  },

  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ currentTab: index });
    this.refreshData();
  },

  fetchOrders() {
    if (this.data.loading) return;
    this.setData({ loading: true });
    wx.showLoading({ title: '加载中' });

    // 策略：统一请求 "current" (进行中) 或 "" (全部)，然后在前端过滤
    // 这样可以解决后端不支持多状态查询的问题
    let apiStatus = '';
    // 如果是 TAB 0(全部)，传空获取所有
    // 如果是 TAB 1/2/3，都属于 "current" 范畴，我们先拉下来再筛，或者分别请求
    // 为了简单且兼容你之前的后端逻辑，我们按如下映射：

    // 但是后端 getUserOrderList 对 "current" 会返回 PENDING, PAID, MAKING, READY
    // 对其他字符串是精确匹配。

    // 方案：
    // Tab 1 (待支付) -> 请求 'PENDING'
    // Tab 2 (制作中) -> 请求 'current' 然后前端过滤出 PAID/MAKING
    // Tab 3 (待取餐) -> 请求 'current' 然后前端过滤出 READY
    // Tab 0 (全部)   -> 请求 '' (后端可能不支持空，或者返回所有)

    // 简化方案：直接用 'current' (进行中) 和 'history' (已完成)，但你的UI是4个Tab。
    // 我们采用：请求所有相关数据，前端过滤。

    let requestStatus = '';
    if (this.data.currentTab === 0) requestStatus = ''; // 全部
    else requestStatus = 'current'; // 1,2,3 都算进行中

    request.get('/app/store/order/list', { status: requestStatus }).then(res => {
      wx.hideLoading();
      this.setData({ loading: false });

      if (res.code === 200) {
        let list = res.data || [];

        // --- 前端过滤逻辑 ---
        if (this.data.currentTab === 1) {
          // 待支付
          list = list.filter(item => item.status === 'PENDING');
        } else if (this.data.currentTab === 2) {
          // 制作中 (已支付 + 制作中)
          list = list.filter(item => item.status === 'PAID' || item.status === 'MAKING');
        } else if (this.data.currentTab === 3) {
          // 待取货 (待取餐 + 待自取)
          list = list.filter(item => item.status === 'READY' || item.status === 'WAIT_PICKUP');
        }
        // Tab 0 不过滤，显示所有

        // 格式化数据
        const displayList = list.map(item => this.processItem(item));

        this.setData({
          orderList: displayList,
          hasMore: false // 这种全量拉取方式，暂不支持分页
        });
      }
    }).catch(err => {
      wx.hideLoading();
      this.setData({ loading: false });
      console.error(err);
    });
  },

  processItem(item) {
    let statusText = item.status;
    let statusStyle = 'completed';

    // 状态文案映射
    const statusMap = {
      'PENDING': '待支付',
      'PAID': '制作中',     // 已支付等待制作
      'MAKING': '制作中',
      'READY': '待取餐',
      'WAIT_PICKUP': '待取餐',
      'COMPLETED': '已完成',
      'CANCELLED': '已取消'
    };
    statusText = statusMap[item.status] || item.status;

    // 样式映射
    if (item.status === 'PENDING') statusStyle = 'pending'; // 橙色/红色
    else if (['PAID', 'MAKING'].includes(item.status)) statusStyle = 'processing'; // 蓝色
    else if (['READY', 'WAIT_PICKUP'].includes(item.status)) statusStyle = 'ready'; // 绿色

    // 图片处理 (取第一张图)
    let productImg = '/assets/images/logo.png';
    const productList = item.products || item.items || [];
    if (productList.length > 0) {
      const p = productList[0];
      if (p.productImage) {
        productImg = p.productImage;
        if (!productImg.startsWith('http') && !productImg.startsWith('/assets')) {
          productImg = 'http://localhost:8080' + productImg;
        }
      }
    }

    return {
      ...item,
      statusText,
      statusStyle,
      productImage: productImg,
      desc: productList.map(p => p.productName).join('、')
    };
  },

  // 支付、取消等功能保持不变，复用你原有的或之前的逻辑...
  payOrder(e) {
    const orderNo = e.currentTarget.dataset.id; // 注意 wxml 传的是 orderNo 还是 id
    // ... (支付逻辑)
    request.post('/app/store/order/pay', { orderNo }).then(res => {
      if(res.code === 200) {
        wx.showToast({ title: '支付成功' });
        this.refreshData();
      }
    });
  },

  cancelOrder(e) {
    const orderNo = e.currentTarget.dataset.id;
    // ... (取消逻辑)
    request.post('/app/store/order/cancel', { orderNo }).then(res => {
      if(res.code === 200) {
        wx.showToast({ title: '已取消' });
        this.refreshData();
      }
    });
  },

  goToDetail(e) {
    const orderNo = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/orders/detail/detail?orderNo=${orderNo}` });
  }
});