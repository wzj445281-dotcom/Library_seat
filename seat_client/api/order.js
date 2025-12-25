const request = require('../utils/request.js')

module.exports = {
    // 获取我的订单列表
    getMyOrders(status) {
        return request.get('/api/app/store/order/list', { status })
    },

    // 获取订单详情
    getOrderDetail(orderNo) {
        return request.get('/api/app/store/order/detail', { orderNo })
    },

    // 取消订单
    cancelOrder(orderNo) {
        return request.post('/api/app/store/order/cancel', { orderNo })
    },

    // 模拟支付 (如果在列表页直接支付)
    payOrder(orderNo) {
        return request.post('/api/app/store/order/pay', { orderNo })
    }
}