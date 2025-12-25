// 基础URL，开发环境通常指向本地，上线后替换为真实域名
// 【本地开发】后端默认端口是8080，所以用 http://localhost:8080/api
// 【真机调试】必须使用局域网IP，不能用localhost (如 http://192.168.1.5:8080/api)
//   获取本机IP：Windows用 ipconfig，Mac/Linux用 ifconfig，找到 IPv4 地址
//   修改下面的 BASE_URL 为你的局域网IP，例如：http://192.168.1.5:8080/api
// 【Docker部署】后端端口映射为8081，用 http://你的IP:8081/api
const BASE_URL = 'http://localhost:8080/api';

const request = (url, method, data) => {
    return new Promise((resolve, reject) => {
        // 1. 获取 Token
        const token = wx.getStorageSync('token');

        // 2. 构造 Header
        let header = {
            'content-type': 'application/json'
        };
        if (token) {
            // 这里的 'Bearer ' 是 JWT 标准前缀，需与后端确认
            header['Authorization'] = 'Bearer ' + token;
        }

        // 处理 GET 请求的 query 参数
        let requestUrl = BASE_URL + url;
        if (method === 'GET' && data) {
            const queryString = Object.keys(data)
                .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
                .join('&');
            if (queryString) {
                requestUrl += (url.includes('?') ? '&' : '?') + queryString;
            }
        }

        wx.request({
            url: requestUrl,
            method: method,
            data: method === 'GET' ? null : data, // GET 请求不传 data
            header: header,
            success(res) {
                // 3. 统一处理响应状态
                if (res.statusCode === 200) {
                    // 业务逻辑成功
                    if (res.data.code === 200) {
                        resolve(res.data);
                    }
                    // 401: Token 过期或无效
                    else if (res.data.code === 401) {
                        // 清除旧 Token
                        wx.removeStorageSync('token');
                        // 触发 App.js 中的重登录方法
                        const app = getApp();
                        if (app && app.doLogin) {
                            app.doLogin();
                        }
                        reject(res.data);
                    } else {
                        // 其他业务错误，如库存不足
                        wx.showToast({
                            title: res.data.message || '系统繁忙',
                            icon: 'none'
                        });
                        reject(res.data);
                    }
                } else {
                    // HTTP 错误 (404, 500 等)
                    wx.showToast({
                        title: '网络服务异常',
                        icon: 'none'
                    });
                    reject(res);
                }
            },
            fail(err) {
                wx.showToast({
                    title: '网络连接失败',
                    icon: 'none'
                });
                reject(err);
            }
        });
    });
};

module.exports = {
    get: (url, data) => request(url, 'GET', data),
    post: (url, data) => request(url, 'POST', data),
    put: (url, data) => request(url, 'PUT', data),
    delete: (url, data) => request(url, 'DELETE', data),
    BASE_URL // 导出以便其他地方使用
};