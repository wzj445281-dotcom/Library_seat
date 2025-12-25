const OrderAPI = require('../../api/order.js');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    currentTab: 0, // 当前选中的 Tab 索引
    // Tab 配置：根据瑞幸风格，通常重点关注 "全部", "待取餐", "已完成"
    tabs: [
      { id: 0, name: '全部', status: null },
      { id: 1, name: '待取餐', status: 2 },
      { id: 2, name: '已完成', status: 3 }
    ],
    orders: [],       // 订单列表数据
    loading: false,   // 加载中状态
    page: 1,          // 当前页码
    pageSize: 10,     // 每页数量
    hasMore: true,    // 是否还有更多数据
    isRefreshing: false // 是否正在下拉刷新
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    // 每次进入页面（包括从详情页返回），刷新列表以获取最新状态
    // 例如：在详情页看完“已取餐”，返回列表应变为“已完成”
    this.refreshList();
  },

  // 切换 Tab 事件
  onTabClick: function (e) {
    const index = e.currentTarget.dataset.index;
    if (this.data.currentTab === index) return;

    this.setData({
      currentTab: index,
      orders: [],     // 切换时先清空，避免显示旧数据
      page: 1,
      hasMore: true,
      loading: true   // 显示 loading 骨架屏或转圈
    }, () => {
      this.loadOrderList();
    });
  },

  // 下拉刷新
  onPullDownRefresh: function () {
    this.refreshList();
  },

  // 触底加载更多
  onReachBottom: function () {
    this.loadOrderList();
  },

  // 刷新列表（重置分页）
  refreshList: function () {
    this.setData({
      page: 1,
      hasMore: true,
      isRefreshing: true
    }, () => {
      this.loadOrderList().then(() => {
        wx.stopPullDownRefresh(); // 停止下拉动画
        this.setData({ isRefreshing: false });
      });
    });
  },

  // 核心：加载数据逻辑
  loadOrderList: function () {
    if (!this.data.hasMore && !this.data.isRefreshing) return Promise.resolve();
    if (this.data.loading && !this.data.isRefreshing && this.data.page > 1) return Promise.resolve();

    this.setData({ loading: true });

    const currentTabItem = this.data.tabs[this.data.currentTab];

    // 构建查询参数
    const params = {
      page: this.data.page,
      size: this.data.pageSize,
      status: currentTabItem.status // status 为 null 时后端应查全部
    };

    return OrderAPI.getOrderList(params)
        .then(res => {
          // 兼容处理：后端可能返回 res.records 或 res.list，根据实际接口调整
          const list = res.records || res.list || [];

          // 数据适配层 (Adapter): 将后端字段转为 WXML 需要的字段
          const adaptedList = list.map(item => {
            // 计算商品总数
            const totalCount = item.items ? item.items.reduce((sum, it) => sum + it.quantity, 0) : 0;
            // 获取第一个商品作为封面
            const firstItem = item.items && item.items.length > 0 ? item.items[0] : {};

            return {
              id: item.id,
              storeName: item.storeName || '瑞幸咖啡 (默认门店)',

              // 状态展示逻辑
              status: item.status,
              statusText: this.getStatusText(item.status),
              // 待取餐(2)显示蓝色，其他显示灰色/黑色，符合 UI 规范
              statusColor: item.status === 2 ? '#0022AB' : '#333333',

              // 取餐码逻辑
              showPickupCode: item.status === 2,
              pickupCode: item.pickupCode || item.fetchCode, // 兼容字段

              // 商品信息
              goodsName: firstItem.productName || '美味饮品',
              goodsDesc: item.items.length > 1 ? `等${totalCount}件商品` : firstItem.spec || '',
              goodsImage: firstItem.productImage || '/assets/images/book-default.png',

              // 金额
              realAmount: item.realAmount || item.totalAmount,
              createTime: item.createTime
            };
          });

          // 如果是第一页，直接覆盖；否则追加
          const newOrders = this.data.page === 1 ? adaptedList : this.data.orders.concat(adaptedList);

          this.setData({
            orders: newOrders,
            page: this.data.page + 1,
            hasMore: list.length >= this.data.pageSize, // 如果返回数量小于 pageSize，说明没数据了
            loading: false
          });
        })
        .catch(err => {
          console.error('加载订单列表失败', err);
          this.setData({ loading: false });
          if(this.data.page === 1) {
            // 第一次加载失败，可以展示空状态或错误提示
            // this.setData({ orders: [] });
          }
          wx.showToast({ title: '数据加载失败', icon: 'none' });
        });
  },

  // 状态文案辅助函数
  getStatusText(status) {
    const map = {
      0: '待支付',
      1: '制作中',
      2: '待取餐',
      3: '已完成',
      4: '已取消'
    };
    return map[status] || '未知状态';
  },

  // 跳转到详情页
  goToDetail: function (e) {
    const id = e.currentTarget.dataset.id;
    if(!id) return;
    wx.navigateTo({
      url: `/pages/orders/detail/detail?id=${id}`,
    });
  },

  // 取消订单
  cancelOrder: function (e) {
    const orderId = e.currentTarget.dataset.id;
    if(!orderId) return;

    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '取消中...' });
          
          OrderAPI.cancelOrder(orderId)
            .then(res => {
              wx.hideLoading();
              if (res.code === 200 || res.success) {
                wx.showToast({ title: '订单已取消', icon: 'success' });
                // 刷新列表
                this.refreshList();
              } else {
                wx.showToast({ 
                  title: res.message || '取消失败', 
                  icon: 'none' 
                });
              }
            })
            .catch(err => {
              wx.hideLoading();
              console.error('取消订单失败:', err);
              wx.showToast({ 
                title: '网络错误，请重试', 
                icon: 'none' 
              });
            });
        }
      }
    });
  },

  // 再来一单
  onOrderAgain: function (e) {
    wx.switchTab({
      url: '/pages/menu/index'
    });
  },

  // 去喝一杯
  onGoToMenu: function (e) {
    wx.switchTab({
      url: '/pages/menu/index'
    });
  }
});