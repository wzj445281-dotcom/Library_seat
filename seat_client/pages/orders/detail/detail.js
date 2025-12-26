const OrderAPI = require('../../../api/order.js');
const app = getApp();
const { request } = require('../../../utils/request');
const wsManager = require('../../../utils/websocket.js');

Page({
    data: {
        orderId: '',
        order: {},
        loading: true,
        steps: [],
        favoriteMap: {},
    },

    onLoad: function (options) {
        const orderNo = options.orderNo || options.id;
        if (orderNo) {
            this.setData({ orderId: orderNo });
            this.loadOrderDetail(orderNo);
            if (app.globalData.token) {
                this.fetchFavoriteIds();
            }
            this.connectWebSocket(orderNo);
        } else {
            wx.showToast({ title: '订单参数错误', icon: 'none' });
        }
    },

    onUnload: function() {
        wsManager.close();
    },

    // --- 加载数据 ---
    loadOrderDetail: function (orderNo) {
        this.setData({ loading: true });
        wx.showLoading({ title: '加载中' });

        OrderAPI.getOrderDetail(orderNo).then(res => {
            if (res.code === 200 && res.data) {
                const backendData = res.data;
                const status = backendData.status || '';

                // 处理商品列表和图片
                const items = (backendData.products || backendData.items || []).map(item => {
                    let imageUrl = item.productImage || item.image || item.imgUrl || '';
                    if (imageUrl && imageUrl.startsWith('/static/')) {
                        imageUrl = 'http://localhost:8080' + imageUrl;
                    }
                    return {
                        ...item,
                        productName: item.productName || item.name,
                        productImage: imageUrl,
                        // 处理价格和数量，防止 null
                        price: item.price || 0,
                        quantity: item.quantity || item.count || 0
                    };
                });

                const order = {
                    id: backendData.id,
                    status: status,
                    statusText: this.getStatusText(status),
                    pickupCode: backendData.pickupCode || '---',
                    storeName: backendData.storeName || '瑞幸咖啡 (科技园店)',
                    storeAddress: backendData.storeAddress || '高新南九道10号',
                    totalAmount: backendData.totalAmount,
                    discountAmount: backendData.discountAmount || 0,
                    realAmount: backendData.totalAmount, // 实付金额通常等于总金额减去优惠，或直接用后端给的字段
                    createTime: backendData.createTime,
                    orderNo: backendData.orderNo,
                    remark: backendData.remark || '无',
                    items: items
                };

                const steps = this.updateSteps(order);

                this.setData({
                    order: order,
                    steps: steps,
                    loading: false
                });
            } else {
                this.handleLoadError(res.message);
            }
        }).catch(err => {
            console.error('详情加载失败', err);
            this.handleLoadError('加载失败，请检查网络');
        }).finally(() => {
            wx.hideLoading();
        });
    },

    // --- 支付与取消逻辑 (新增) ---

    /**
     * 立即支付
     */
    onPay() {
        const orderNo = this.data.order.orderNo;
        if (!orderNo) return;

        wx.showLoading({ title: '支付中...' });

        // 模拟支付过程
        OrderAPI.payOrder({ orderNo: orderNo }).then(res => {
            wx.hideLoading();
            if (res.code === 200) {
                wx.showToast({ title: '支付成功', icon: 'success' });
                // 刷新页面状态
                this.loadOrderDetail(orderNo);
            } else {
                wx.showToast({ title: res.message || '支付失败', icon: 'none' });
            }
        }).catch(err => {
            wx.hideLoading();
            wx.showToast({ title: '支付异常', icon: 'none' });
        });
    },

    /**
     * 取消订单
     */
    onCancelOrder() {
        const orderNo = this.data.order.orderNo;
        if (!orderNo) return;

        wx.showModal({
            title: '提示',
            content: '确定要取消当前订单吗？',
            confirmColor: '#0022AB',
            success: (res) => {
                if (res.confirm) {
                    wx.showLoading({ title: '取消中' });
                    OrderAPI.cancelOrder({ orderNo: orderNo }).then(res => {
                        wx.hideLoading();
                        if (res.code === 200) {
                            wx.showToast({ title: '订单已取消', icon: 'none' });
                            this.loadOrderDetail(orderNo);
                        } else {
                            wx.showToast({ title: res.message || '取消失败', icon: 'none' });
                        }
                    });
                }
            }
        });
    },

    // --- 原有辅助方法 ---

    updateSteps: function(order) {
        let steps = [
            { text: '已下单', time: '', active: false },
            { text: '制作中', time: '', active: false },
            { text: '请取餐', time: '', active: false },
        ];

        const timeStr = order.createTime ? order.createTime.substring(11, 16) : '';
        steps[0].active = true;
        steps[0].time = timeStr;

        if (['PAID', 'MAKING'].includes(order.status)) {
            steps[1].active = true;
            steps[1].time = timeStr; // 实际应为支付时间
        }

        if (['READY', 'WAIT_PICKUP'].includes(order.status)) {
            steps[1].active = true;
            steps[2].active = true;
            steps[2].time = '现在';
        }

        if (order.status === 'COMPLETED') {
            steps[1].active = true;
            steps[2].active = true;
        }

        return steps;
    },

    handleLoadError: function(msg) {
        wx.showToast({ title: msg || '加载失败', icon: 'none' });
        this.setData({
            loading: false,
            order: { items: [] }
        });
    },

    getStatusText: function(status) {
        const statusMap = {
            'PENDING': '待支付',
            'PENDING_PAY': '待支付',
            'PAID': '制作中',
            'MAKING': '制作中',
            'READY': '待取餐',
            'WAIT_PICKUP': '待取餐',
            'COMPLETED': '已完成',
            'CANCELLED': '已取消',
            'REFUNDED': '已退款'
        };
        return statusMap[status] || status;
    },

    onCallStore: function () {
        wx.makePhoneCall({ phoneNumber: '13800000000' });
    },

    onOrderAgain: function () {
        const order = this.data.order;
        if (!order || !order.items || order.items.length === 0) {
            wx.showToast({ title: '订单数据缺失', icon: 'none' });
            return;
        };

        wx.showLoading({ title: '加入购物车...' });
        try {
            let cart = wx.getStorageSync('cart') || [];
            order.items.forEach(orderItem => {
                // 这里的 productId 需对应 orderItem 里的 productId
                const existingIndex = cart.findIndex(c => c.id === orderItem.productId);

                // 简单处理：不判断规格直接加数量，或者视为新条目
                // 如果需要严格规格匹配，需要后端返回 spec
                if (existingIndex > -1) {
                    cart[existingIndex].quantity += (orderItem.quantity || 1);
                } else {
                    cart.push({
                        id: orderItem.productId,
                        name: orderItem.productName,
                        pic: orderItem.productImage,
                        price: orderItem.price,
                        quantity: (orderItem.quantity || 1),
                        checked: true
                    });
                }
            });
            wx.setStorageSync('cart', cart);
            setTimeout(() => {
                wx.hideLoading();
                wx.switchTab({ url: '/pages/menu/index' });
            }, 500);
        } catch (err) {
            console.error(err);
            wx.hideLoading();
        }
    },

    fetchFavoriteIds() {
        request.get('/app/favorite/ids').then(res => {
            if (res.code === 200) {
                const map = {};
                res.data.forEach(id => map[id] = true);
                this.setData({ favoriteMap: map });
            }
        });
    },

    connectWebSocket(orderNo) {
        wsManager.connect(orderNo, (data) => {
            if (data.type === 'ORDER_STATUS_UPDATE' && data.orderNo === orderNo) {
                const order = this.data.order;
                order.status = data.status;
                order.statusText = this.getStatusText(data.status);
                const steps = this.updateSteps(order);
                this.setData({ order, steps });
                wx.showToast({ title: '订单状态更新', icon: 'success' });
            }
        });
    }
});