import request from '../utils/request.js';

// 获取座位布局
export const getSeatLayout = () => {
    return request('/seat/layout', 'GET');
};