const request = require('../utils/request.js');

module.exports = {
    /**
     * 创建订单
     */
    createOrder(data) {
        return request.post('/app/store/order/create', data);
    },

    /**
     * 获取订单详情
     */
    getOrderDetail(orderNo) {
        return request.get('/app/store/order/detail', { orderNo: orderNo });
    },

    /**
     * 获取订单列表
     */
    getOrderList(status) {
        return request.get('/app/store/order/list', { status });
    },

    /**
     * 支付订单
     */
    payOrder(data) {
        return request.post('/app/store/order/pay', data);
    },

    /**
     * 取消订单
     */
    cancelOrder(data) {
        return request.post('/app/store/order/cancel', data);
    }
};