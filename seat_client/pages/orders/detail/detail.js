const OrderAPI = require('../../../api/order.js');
// 引入二维码生成库 (请确保 utils/QRCODE.js 文件存在)
const QRCode = require('../../../utils/QRCODE.js');
const app = getApp();
const { request } = require('../../utils/request');

Page({
    data: {
        orderId: '',
        order: {}, // 默认初始化为空对象，防止 wxml 访问 items 时崩溃
        loading: true,
        steps: [
            { text: '已下单', time: '', active: false },
            { text: '制作中', time: '', active: false },
            { text: '请取餐', time: '', active: false },
        ],
        // 收藏相关数据
        favoriteMap: {}, // 用对象存储收藏状态，key为productId，value为true，O(1)查找
    },

    onLoad: function (options) {
        const orderId = options.id;
        if (orderId) {
            this.setData({ orderId });
            this.loadOrderDetail(orderId);
            // 获取收藏状态
            if (app.globalData.token) {
                this.fetchFavoriteIds();
            }
        } else {
            wx.showToast({ title: '订单参数错误', icon: 'none' });
        }
    },

    /**
     * 加载订单详情
     */
    loadOrderDetail: function (orderId) {
        this.setData({ loading: true });
        wx.showLoading({ title: '加载中' });

        OrderAPI.getOrderDetail(orderId).then(res => {
            // 处理后端返回结构：假设 res.code 为 200 且数据在 res.data 中
            if (res.code === 200 && res.data) {
                const backendData = res.data;

                // 1. 数据映射 (适配后端 Entity 字段)
                const order = {
                    id: backendData.id,
                    status: backendData.status, // 0:待支付, 1:制作中, 2:待取餐, 3:已完成, 4:已取消
                    statusText: this.getStatusText(backendData.status),
                    pickupCode: backendData.pickupCode || backendData.fetchCode || '---',
                    storeName: backendData.storeName || '瑞幸咖啡 (科技园店)',
                    storeAddress: backendData.storeAddress || '高新南九道10号',
                    totalAmount: backendData.totalAmount,
                    payAmount: backendData.payAmount || backendData.totalAmount,
                    createTime: backendData.createTime,
                    orderNo: backendData.orderNo,
                    remark: backendData.remark || '无',
                    items: backendData.items || []
                };

                // 2. 更新时间轴状态
                let steps = this.updateSteps(order);

                // 3. 渲染页面
                this.setData({
                    order: order,
                    steps: steps,
                    loading: false
                }, () => {
                    // 如果状态是“待取餐”(2)，绘制二维码
                    if (order.status === 2) {
                        this.drawQrCode(order.pickupCode);
                    }
                });
            } else {
                this.handleLoadError(res.message);
            }
        }).catch(err => {
            console.error('获取订单详情失败', err);
            this.handleLoadError('权限不足或网络异常(403)');
        }).finally(() => {
            wx.hideLoading();
        });
    },

    /**
     * 更新进度条状态逻辑
     */
    updateSteps: function(order) {
        let steps = [
            { text: '已下单', time: '', active: false },
            { text: '制作中', time: '', active: false },
            { text: '请取餐', time: '', active: false },
        ];

        const timeStr = order.createTime ? order.createTime.substring(11, 16) : '';

        // 步骤1: 已下单
        steps[0].active = true;
        steps[0].time = timeStr;

        // 步骤2: 制作中 (状态为1, 2, 3均视为已开始或完成制作)
        if (order.status >= 1 && order.status <= 3) {
            steps[1].active = true;
            steps[1].time = timeStr;
        }

        // 步骤3: 待取餐 (状态为2或3)
        if (order.status === 2 || order.status === 3) {
            steps[2].active = true;
            steps[2].time = order.status === 2 ? '现在' : '';
        }

        return steps;
    },

    /**
     * 错误处理，防止页面崩溃
     */
    handleLoadError: function(msg) {
        wx.showToast({ title: msg || '加载失败', icon: 'none' });
        this.setData({
            loading: false,
            order: { items: [] } // 设置空对象防止 items.length 报错
        });
    },

    getStatusText: function(status) {
        const map = { 0: '待支付', 1: '制作中', 2: '待取餐', 3: '已完成', 4: '已取消' };
        return map[status] || '处理中';
    },

    /**
     * 绘制取餐二维码
     */
    drawQrCode: function (code) {
        if (!code || code === '---') return;
        console.log('开始绘制二维码:', code);
        try {
            // 注意：需确保 wxml 中有 <canvas canvas-id="myQrcode"></canvas>
            new QRCode('myQrcode', {
                text: String(code),
                width: 160,
                height: 160,
                colorDark: "#0022AB", // 瑞幸蓝二维码
                colorLight: "#ffffff",
                correctLevel: QRCode.CorrectLevel.H
            });
        } catch (e) {
            console.error('二维码绘制异常:', e);
        }
    },

    /**
     * 二维码点击放大预览
     */
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
                wx.showToast({ title: '预览失败', icon: 'none' });
            }
        }, this);
    },

    /**
     * 拨打门店电话
     */
    onCallStore: function () {
        wx.makePhoneCall({
            phoneNumber: '13800000000', // 实际应从 order 数据中获取
        });
    },

    /**
     * “再来一单”：将商品重新存入购物车并跳转
     */
    onOrderAgain: function () {
        const order = this.data.order;
        if (!order || !order.items || order.items.length === 0) {
            wx.showToast({ title: '订单数据未加载', icon: 'none' });
            return;
        };

        wx.showLoading({ title: '正在加入购物车' });

        try {
            // 获取当前购物车缓存
            let cart = wx.getStorageSync('cart') || [];

            order.items.forEach(orderItem => {
                // 查找购物车是否已有同款（同ID且同规格）
                const existingIndex = cart.findIndex(c => c.id === orderItem.productId && c.spec === orderItem.spec);

                if (existingIndex > -1) {
                    cart[existingIndex].quantity += orderItem.quantity;
                } else {
                    cart.push({
                        id: orderItem.productId,
                        name: orderItem.productName,
                        pic: orderItem.productImage,
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
            }, 600);
        } catch (err) {
            console.error('加入购物车失败:', err);
            wx.hideLoading();
        }
    },

    /**
     * 1. 获取用户所有收藏的商品ID
     * 接口: GET /api/app/favorite/ids
     */
    fetchFavoriteIds() {
        request({
            url: '/api/app/favorite/ids',
            method: 'GET'
        }).then(res => {
            if (res.code === 200) {
                // 将数组转换为 Map 结构 {101: true, 102: true}，方便 WXML 判断
                const map = {};
                res.data.forEach(id => {
                    map[id] = true;
                });
                
                this.setData({
                    favoriteMap: map
                });
            }
        }).catch(err => {
            console.error('获取收藏列表失败', err);
        });
    },

    /**
     * 2. 核心交互：点击爱心收藏/取消
     * 接口: POST /api/app/favorite/toggle
     */
    onToggleFavorite(e) {
        // 震动反馈，提升手感 (瑞幸风格)
        wx.vibrateShort({ type: 'light' });

        const { id, index, categoryIndex } = e.currentTarget.dataset;
        
        // === 乐观更新 (Optimistic UI) ===
        // 不等接口返回，直接修改前端状态，让用户感觉"零延迟"
        const isFavorite = !!this.data.favoriteMap[id];
        const newStatus = !isFavorite;
        
        const key = `favoriteMap.${id}`;
        this.setData({
            [key]: newStatus
        });

        // 发送请求
        request({
            url: '/api/app/favorite/toggle',
            method: 'POST',
            data: { productId: id }
        }).then(res => {
            if (res.code !== 200) {
                // 如果失败，回滚状态
                this.setData({ [key]: isFavorite });
                wx.showToast({ title: '操作失败', icon: 'none' });
            }
        }).catch(() => {
            // 网络错误回滚
            this.setData({ [key]: isFavorite });
        });
    }
});