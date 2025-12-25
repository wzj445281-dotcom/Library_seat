const OrderAPI = require('../../../api/order.js');
// 如果你有真实的二维码库，请在这里引入
// const QRCode = require('../../../utils/qrcode.js');

Page({
    data: {
        order: null,
        loading: true,
        steps: [
            { text: '已下单', time: '10:00', active: true },
            { text: '制作中', time: '10:01', active: true },
            { text: '请取餐', time: '', active: false },
        ]
    },

    onLoad: function (options) {
        const orderId = options.id;
        this.loadOrderDetail(orderId);
    },

    loadOrderDetail: function (orderId) {
        // 模拟数据
        const mockOrder = {
            id: orderId || 12345,
            status: 2,
            statusText: '待取餐',
            pickupCode: '888',
            storeName: '瑞幸咖啡 (软件园店)',
            storeAddress: '高新区天府大道中段1号软件园D区大堂',
            totalAmount: 32.00,
            discountAmount: 12.00,
            realAmount: 20.00,
            createTime: '2023-10-27 10:00:00',
            orderNo: 'LK231027888888',
            items: [
                {
                    id: 1,
                    productName: '生椰拿铁',
                    productImage: '/assets/images/book-default.png',
                    spec: '冰/不加糖/标准',
                    price: 18.00,
                    quantity: 1
                },
                {
                    id: 2,
                    productName: '厚乳拿铁',
                    productImage: '/assets/images/book-default.png',
                    spec: '热/半糖',
                    price: 14.00,
                    quantity: 1
                }
            ]
        };

        let steps = this.data.steps;
        if (mockOrder.status === 2) {
            steps[2].active = true;
            steps[2].time = '10:05';
        }

        setTimeout(() => {
            this.setData({
                order: mockOrder,
                steps: steps,
                loading: false
            }, () => {
                // 数据加载完成后，如果是待取餐状态，绘制二维码
                if (mockOrder.status === 2) {
                    this.drawQrCode(mockOrder.pickupCode);
                }
            });
        }, 500);
    },

    drawQrCode: function (code) {
        // 实际项目中调用 weapp-qrcode.js
        // QRCode.draw(code, 'myQrcode', this);

        // 这里暂时不做真实绘制，因为没有引入库文件
        // 页面上使用 image 占位即可
        console.log('Drawing QR Code for:', code);
    },

    // 放大二维码
    onShowQrCode: function () {
        wx.previewImage({
            urls: ['/assets/images/seat-active.png'], // 替换为真实的二维码临时路径
            current: '/assets/images/seat-active.png'
        });
    },

    onCallStore: function () {
        wx.makePhoneCall({
            phoneNumber: '13800000000',
        });
    },

    onOrderAgain: function () {
        wx.switchTab({
            url: '/pages/menu/index',
        });
    }
});