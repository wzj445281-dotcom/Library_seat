import config from '../config/config.js'

/**
 * 封装 wx.request
 * @param {String} url 请求地址 (如 /auth/login)
 * @param {String} method GET/POST/PUT/DELETE
 * @param {Object} data 请求参数
 * @param {Boolean} needLogin 是否需要登录权限 (默认true)
 */
const request = (url, method = 'GET', data = {}, needLogin = true) => {
    return new Promise((resolve, reject) => {

        let header = {
            'content-type': 'application/json' // 默认 JSON
        };

        // 1. 自动携带 Token
        if (needLogin) {
            const token = wx.getStorageSync('token');
            if (token) {
                header['Authorization'] = 'Bearer ' + token;
            } else {
                // 如果没Token但接口需要登录，直接跳转
                wx.navigateTo({ url: '/pages/login/login' });
                return reject('未登录');
            }
        }

        wx.request({
            url: config.BASE_URL + url,
            method: method,
            data: data,
            header: header,
            success: (res) => {
                // 2. 统一拦截处理
                if (res.statusCode === 200) {
                    // 后端业务逻辑成功 (ApiResponse.code = 200)
                    if (res.data.code === 200) {
                        resolve(res.data.data);
                    } else {
                        wx.showToast({ title: res.data.message || '操作失败', icon: 'none' });
                        reject(res.data);
                    }
                } else if (res.statusCode === 403) {
                    // 3. Token 过期处理
                    wx.showToast({ title: '登录过期，请重新登录', icon: 'none' });
                    wx.removeStorageSync('token');
                    setTimeout(() => {
                        wx.redirectTo({ url: '/pages/login/login' });
                    }, 1500);
                    reject('Token Expired');
                } else {
                    wx.showToast({ title: '服务器开小差了', icon: 'none' });
                    reject(res);
                }
            },
            fail: (err) => {
                wx.showToast({ title: '网络连接失败', icon: 'none' });
                reject(err);
            }
        });
    });
}

export default request;