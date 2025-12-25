import request from '../utils/request'

export default {
    /**
     * 创建商品订单
     * @param {object} data { productId, count, amount }
     */
    createOrder(data) {
        return request({
            url: '/api/app/order/create',
            method: 'POST',
            data: data
        })
    },

    /**
     * 获取我的订单列表
     */
    getMyOrders(status) {
        return request({
            url: '/api/app/order/list',
            method: 'GET',
            data: { status }
        })
    },

    /**
     * 支付订单 (模拟)
     */
    payOrder(orderId) {
        return request({
            url: `/api/app/order/pay/${orderId}`,
            method: 'POST'
        })
    }
}