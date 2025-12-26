const request = require('../utils/request.js');

module.exports = {
    /**
     * 微信登录
     * @param {string} code 微信 login 获取的 code
     * @param {object} userInfo (可选) 用户信息，用于注册
     */
    login(code, userInfo) {
        return request.post('/app/auth/login', {
            code: code,
            userInfo: userInfo
        });
    },

    /**
     * 退出登录
     */
    logout() {
        return request.post('/app/auth/logout');
    },

    /**
     * 答辩演示专用登录 - 绕过微信验证，一键模拟登录
     * ⚠️ 仅用于答辩演示环境，生产环境请删除此函数
     */
    mockLogin() {
        return request.post('/app/auth/mock-login');
    },

    /**
     * 账号密码登录
     * 用于答辩演示环境，替代微信登录
     * @param {object} data {username: string, password: string}
     */
    accountLogin(data) {
        return request.post('/app/auth/login/account', data);
    },

    /**
     * 账号密码注册
     * 用于答辩演示环境，替代微信登录
     * @param {object} data {username: string, password: string, nickname: string}
     */
    accountRegister(data) {
        return request.post('/app/auth/register/account', data);
    }
};