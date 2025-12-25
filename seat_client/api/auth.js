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
    }
};