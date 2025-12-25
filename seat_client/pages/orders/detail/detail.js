const OrderAPI = require('../../../api/order.js');
// 引入二维码生成库
const QRCode = require('../../../utils/QRCODE.js');

Page({
    data: {
        order: null,
        loading: true,
        steps: [
            { text: '已下单', time: '', active: false },
            { text: '制作中', time: '', active: false },
            { text: '请取餐', time: '', active: false },
        ]
    },

    onLoad: function (options) {
        const orderId = options.id;
        // 如果没有 ID，通常是测试，可以给一个默认值或报错
        if(orderId) {
            this.loadOrderDetail(orderId);
        } else {
            wx.showToast({ title: '订单参数错误', icon: 'none' });
        }
    },

    loadOrderDetail: function (orderId) {
        this.setData({ loading: true });

        // 1. 调用真实接口
        OrderAPI.getOrderDetail(orderId).then(res => {
            // 注意：这里假设 res 是后端返回的完整数据对象
            // 如果您的 request.js 封装里 res.data 才是数据，请改为 const data = res.data;
            const backendData = res;

            // 2. 数据适配 (Adapter Pattern)
            // 将后端字段映射为前端 UI 所需的字段
            const order = {
                id: backendData.id,
                status: backendData.status, // 假设 1:已下单, 2:待取餐, 3:已完成
                statusText: this.getStatusText(backendData.status),
                pickupCode: backendData.pickupCode || backendData.fetchCode || '---', // 兼容不同字段名
                storeName: backendData.storeName || '瑞幸咖啡',
                storeAddress: backendData.storeAddress || '',
                totalAmount: backendData.totalAmount,
                discountAmount: backendData.discountAmount || 0,
                realAmount: backendData.realAmount || backendData.payAmount,
                createTime: backendData.createTime,
                orderNo: backendData.orderNo,
                items: backendData.items || [] // 确保 items 是数组
            };

            // 3. 更新时间轴状态 (Timeline)
            let steps = this.data.steps;

            // 步骤1: 已下单 (总是激活)
            steps[0].active = true;
            steps[0].time = order.createTime ? order.createTime.split(' ')[1] : '';

            // 步骤2: 制作中 (status >= 1)
            if (order.status >= 1) {
                steps[1].active = true;
                // 如果后端没有制作时间，暂时用下单时间+1分钟模拟
                steps[1].time = steps[0].time;
            }

            // 步骤3: 待取餐 (status == 2)
            if (order.status === 2) {
                steps[2].active = true;
                steps[2].time = new Date().toTimeString().substring(0, 5); // 当前时间
            }

            // 4. 渲染数据
            this.setData({
                order: order,
                steps: steps,
                loading: false
            }, () => {
                // 如果是待取餐状态，绘制二维码
                if (order.status === 2) {
                    setTimeout(() => {
                        this.drawQrCode(order.pickupCode);
                    }, 200);
                }
            });

        }).catch(err => {
            console.error('获取订单详情失败', err);
            wx.hideLoading();
            wx.showToast({
                title: '加载失败，请重试',
                icon: 'none'
            });
            // 出错时，为了不让页面空白，可以保留 loading 状态或显示错误页
            this.setData({ loading: false });
        });
    },

    // 辅助方法：状态文案映射
    getStatusText: function(status) {
        const map = {
            0: '待支付',
            1: '制作中',
            2: '待取餐',
            3: '已完成',
            4: '已取消'
        };
        return map[status] || '未知状态';
    },

    drawQrCode: function (code) {
        if(!code) return;
        console.log('开始绘制二维码, 内容:', code);
        try {
            new QRCode('myQrcode', {
                text: String(code),
                width: 160,
                height: 160,
                colorDark: "#000000",
                colorLight: "#ffffff",
                correctLevel: QRCode.CorrectLevel.H
            });
        } catch (e) {
            console.error('绘制二维码失败:', e);
        }
    },

    onShowQrCode: function () {
        wx.canvasToTempFilePath({
            canvasId: 'myQrcode',
            success: (res) => {
                wx.previewImage({
                    urls: [res.tempFilePath],
                    current: res.tempFilePath
                });
            },
            fail: () => {
                wx.previewImage({
                    urls: ['/assets/images/seat-active.png'],
                    current: '/assets/images/seat-active.png'
                });
            }
        }, this);
    },

    onCallStore: function () {
        wx.makePhoneCall({
            phoneNumber: '13800000000',
        });
    },

    onOrderAgain: function () {
        const items = this.data.order.items;
        if (!items || items.length === 0) return;

        wx.showLoading({ title: '正在加入购物车' });

        try {
            let cart = wx.getStorageSync('cart') || [];
            items.forEach(orderItem => {
                const existingIndex = cart.findIndex(c => c.id === orderItem.id && c.spec === orderItem.spec);
                if (existingIndex > -1) {
                    cart[existingIndex].quantity += orderItem.quantity;
                } else {
                    cart.push({
                        id: orderItem.id,
                        name: orderItem.productName,
                        productName: orderItem.productName,
                        pic: orderItem.productImage,
                        productImage: orderItem.productImage,
                        spec: orderItem.spec,
                        price: orderItem.price,
                        quantity: orderItem.quantity,
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
    }
});