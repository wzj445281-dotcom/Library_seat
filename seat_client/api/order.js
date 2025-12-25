const request = require('../utils/request.js');

module.exports = {
    /**
     * 获取订单列表
     * @param {Object} params { status, page, size }
     */
    getOrderList(params) {
        // 请确认后端接口地址，通常是 /app/orders 或 /app/order/list
        return request.get('/app/orders', params);
    },

    /**
     * 获取订单详情
     * @param {String} orderId
     */
    getOrderDetail(orderId) {
        // RESTful 风格接口
        return request.get(`/app/orders/${orderId}`);
    },

    /**
     * 创建订单
     * @param {Object} data
     */
    createOrder(data) {
        return request.post('/app/orders', data);
    },

    /**
     * 取消订单
     * @param {String} orderId
     */
    cancelOrder(orderId) {
        return request.post(`/app/orders/${orderId}/cancel`);
    }
};