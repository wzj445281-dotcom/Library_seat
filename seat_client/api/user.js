import request from '../utils/request.js';

// 获取用户信息 (最新)
export const getUserInfo = () => {
    return request('/app/user/info', 'GET');
};

// 获取信用日志
export const getCreditLogs = () => {
    return request('/app/user/credit-logs', 'GET');
};