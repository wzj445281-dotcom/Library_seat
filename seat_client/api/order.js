const request = require('../utils/request.js');

module.exports = {
    /**
     * 创建订单
     * @param {object} data 订单数据
     * {
     * storeId: 1,
     * items: [{ productId: 1, count: 2, spec: '标准糖' }],
     * remark: '不要辣',
     * diningType: 'self' // self: 自取, delivery: 外卖
     * }
     */
    createOrder(data) {
        return request.post('/api/app/store/order/create', data);
    },

    /**
     * 获取订单详情
     * @param {string} orderNo 订单号
     */
    getOrderDetail(orderNo) {
        return request.get('/api/app/store/order/detail', { orderNo: orderNo });
    },

    /**
     * 获取订单列表
     * @param {string} status 状态筛选 (可选)
     */
    getOrderList(status) {
        return request.get('/api/app/store/order/list', { status });
    }
};