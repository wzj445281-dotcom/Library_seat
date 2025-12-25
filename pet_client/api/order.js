import request from '../utils/request.js';

// 创建订单
export const createOrder = (productId, count) => {
    const data = {
        productId: productId,
        count: count || 1
    };
    return request('/app/shop/order/create', 'POST', data);
};

// 获取我的订单列表
export const getMyOrders = () => {
    return request('/app/orders/list', 'GET');
};

// 确认收货
export const confirmOrder = (id) => {
    return request(`/app/orders/confirm/${id}`, 'POST');
};

// 取消订单
export const cancelOrder = (id) => {
    return request(`/app/orders/cancel/${id}`, 'POST');
};