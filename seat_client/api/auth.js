import request from '../utils/request.js';

// 登录 (不需要 token)
export const login = (studentId, password) => {
    return request('/auth/login', 'POST', { studentId, password }, false);
};

// 注册 (不需要 token)
export const register = (data) => {
    return request('/auth/register', 'POST', data, false);
};