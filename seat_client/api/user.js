const request = require('../utils/request.js');

module.exports = {
    /**
     * 获取用户信息
     */
    getUserInfo() {
        return request.get('/app/user/info');
    },

    /**
     * 获取信用日志
     */
    getCreditLogs() {
        return request.get('/app/user/credit-logs');
    }
};