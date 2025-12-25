const app = getApp();

Page({
    data: {
        orderId: '',
        currentStep: 1, // 进度控制: 0:已接单, 1:制作中, 2:请取餐

        // 模拟订单数据 (Mock)
        order: {
            status: 'MAKING', // 状态枚举: CREATED, MAKING, WAIT_PICKUP, FINISHED, CANCELLED
            statusText: '制作中',
            statusDesc: '饮品制作中，请耐心等待',
            pickupCode: '8802', // 取餐码
            shopName: '瑞幸咖啡 (科技园店)',
            orderNo: '34829102384912',
            createTime: '2025-12-25 10:20:00',
            totalCount: 2,
            totalPrice: 42,
            remark: '不要太烫，谢谢',
            items: [
                {
                    id: 1,
                    name: '生椰拿铁',
                    spec: '标准糖/冰/大杯',
                    count: 1,
                    price: 21,
                    image: 'https://images.unsplash.com/photo-1541167760496-1628856ab772?w=200&h=200&fit=crop'
                },
                {
                    id: 2,
                    name: '美式咖啡',
                    spec: '无糖/热/中杯',
                    count: 1,
                    price: 21,
                    image: 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=200&h=200&fit=crop'
                }
            ]
        }
    },

    onLoad(options) {
        if (options.id) {
            this.setData({ orderId: options.id });
        }

        // 初始化模拟：从"制作中"开始
        this.startSimulation();
    },

    // 模拟订单状态流转 (Demo用途)
    startSimulation() {
        wx.showToast({
            title: '模拟: 制作中...',
            icon: 'loading',
            duration: 1000
        });

        // 3秒后变更为 "请取餐"
        setTimeout(() => {
            this.setData({
                currentStep: 2,
                'order.status': 'WAIT_PICKUP',
                'order.statusText': '请取餐',
                'order.statusDesc': '您的饮品已制作完成，请凭码取餐',
            });

            // 触发震动反馈
            wx.vibrateLong();
            wx.showToast({
                title: '制作完成!',
                icon: 'success'
            });
        }, 3000);
    },

    copyOrderNo() {
        wx.setClipboardData({
            data: this.data.order.orderNo,
            success: () => {
                wx.showToast({ title: '已复制订单号', icon: 'none' });
            }
        });
    }
});