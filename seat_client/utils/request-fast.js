/**
 * 快速请求工具 - 完全本地运行
 * 使用微信开发者工具代理，无需外部网络
 */

// 完全本地运行，使用localhost
const BASE_URL = 'http://localhost:8080/api';

// 始终使用localhost（微信开发者工具会自动代理）
const getBaseUrl = () => {
    return BASE_URL;
};

const request = (url, method, data) => {
    return new Promise((resolve, reject) => {
        const token = wx.getStorageSync('token');
        let header = {
            'content-type': 'application/json'
        };
        if (token) {
            header['Authorization'] = 'Bearer ' + token;
        }

        let requestUrl = getBaseUrl() + url;
        if (method === 'GET' && data) {
            const queryString = Object.keys(data)
                .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
                .join('&');
            if (queryString) {
                requestUrl += (url.includes('?') ? '&' : '?') + queryString;
            }
        }

        const startTime = Date.now();
        
        wx.request({
            url: requestUrl,
            method: method,
            data: method === 'GET' ? null : data,
            header: header,
            timeout: 8000, // 8秒超时（更快）
            success(res) {
                const duration = Date.now() - startTime;
                console.log(`[${method}] ${url} - ${duration}ms`);
                
                if (res.statusCode === 200) {
                    if (res.data.code === 200) {
                        resolve(res.data);
                    } else if (res.data.code === 401) {
                        wx.removeStorageSync('token');
                        const app = getApp();
                        if (app && app.doLogin) {
                            app.doLogin();
                        }
                        reject(res.data);
                    } else {
                        wx.showToast({
                            title: res.data.message || '系统繁忙',
                            icon: 'none',
                            duration: 1500 // 减少提示时间
                        });
                        reject(res.data);
                    }
                } else {
                    wx.showToast({
                        title: '网络服务异常',
                        icon: 'none'
                    });
                    reject(res);
                }
            },
            fail(err) {
                const duration = Date.now() - startTime;
                console.error(`[${method}] ${url} 失败 - ${duration}ms`, err);
                
                wx.showToast({
                    title: '网络连接失败',
                    icon: 'none',
                    duration: 2000
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
    BASE_URL: getBaseUrl()
};

