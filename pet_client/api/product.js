import request from '../utils/request.js';

// 获取商品列表
export const getProductList = (category) => {
    const url = category ? `/app/shop/products?category=${category}` : '/app/shop/products';
    return request(url, 'GET');
};

// 获取商品详情
export const getProductDetail = (id) => {
    return request(`/app/shop/product/${id}`, 'GET');
};