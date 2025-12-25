/**
 * 带缓存的请求工具
 * 用于减少网络请求，降低延迟
 * 完全本地运行，无需外部网络
 */

const BASE_URL = 'http://localhost:8080/api';  // 本地运行，无需网络

// 缓存配置
const CACHE_CONFIG = {
    // 缓存时间（毫秒）
    menu: 5 * 60 * 1000,        // 菜单数据缓存5分钟
    product: 10 * 60 * 1000,    // 商品详情缓存10分钟
    category: 30 * 60 * 1000,   // 分类数据缓存30分钟
    default: 2 * 60 * 1000      // 默认缓存2分钟
};

// 获取缓存键
function getCacheKey(url, data) {
    const dataStr = data ? JSON.stringify(data) : '';
    return `cache_${url}_${dataStr}`;
}

// 获取缓存时间
function getCacheTime(url) {
    if (url.includes('/menu')) return CACHE_CONFIG.menu;
    if (url.includes('/product')) return CACHE_CONFIG.product;
    if (url.includes('/category')) return CACHE_CONFIG.category;
    return CACHE_CONFIG.default;
}

// 从缓存获取
function getFromCache(cacheKey) {
    try {
        const cached = wx.getStorageSync(cacheKey);
        if (cached && cached.expireTime > Date.now()) {
            return cached.data;
        }
        // 缓存过期，删除
        wx.removeStorageSync(cacheKey);
        return null;
    } catch (e) {
        return null;
    }
}

// 保存到缓存
function saveToCache(cacheKey, data, cacheTime) {
    try {
        wx.setStorageSync(cacheKey, {
            data: data,
            expireTime: Date.now() + cacheTime
        });
    } catch (e) {
        console.warn('缓存保存失败:', e);
    }
}

// 请求函数（带缓存）
const request = (url, method, data, useCache = true) => {
    return new Promise((resolve, reject) => {
        const token = wx.getStorageSync('token');
        let header = {
            'content-type': 'application/json'
        };
        if (token) {
            header['Authorization'] = 'Bearer ' + token;
        }

        // GET请求且启用缓存时，先检查缓存
        if (method === 'GET' && useCache) {
            const cacheKey = getCacheKey(url, data);
            const cached = getFromCache(cacheKey);
            if (cached) {
                console.log('使用缓存数据:', url);
                resolve(cached);
                return;
            }
        }

        let requestUrl = BASE_URL + url;
        if (method === 'GET' && data) {
            const queryString = Object.keys(data)
                .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
                .join('&');
            if (queryString) {
                requestUrl += (url.includes('?') ? '&' : '?') + queryString;
            }
        }

        // 设置超时时间（减少等待）
        const startTime = Date.now();
        
        wx.request({
            url: requestUrl,
            method: method,
            data: method === 'GET' ? null : data,
            header: header,
            timeout: 10000, // 10秒超时
            success(res) {
                const duration = Date.now() - startTime;
                console.log(`请求耗时: ${duration}ms`, url);
                
                if (res.statusCode === 200) {
                    if (res.data.code === 200) {
                        // GET请求且启用缓存时，保存到缓存
                        if (method === 'GET' && useCache) {
                            const cacheKey = getCacheKey(url, data);
                            const cacheTime = getCacheTime(url);
                            saveToCache(cacheKey, res.data, cacheTime);
                        }
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
                            icon: 'none'
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
                console.error(`请求失败 (${duration}ms):`, err);
                
                // 网络失败时，如果是GET请求，尝试使用缓存
                if (method === 'GET' && useCache) {
                    const cacheKey = getCacheKey(url, data);
                    const cached = getFromCache(cacheKey);
                    if (cached) {
                        console.log('网络失败，使用缓存数据:', url);
                        resolve(cached);
                        return;
                    }
                }
                
                wx.showToast({
                    title: '网络连接失败: ' + (err.errMsg || '请检查网络'),
                    icon: 'none',
                    duration: 2000 // 减少提示时间
                });
                reject(err);
            }
        });
    });
};

module.exports = {
    get: (url, data, useCache = true) => request(url, 'GET', data, useCache),
    post: (url, data) => request(url, 'POST', data, false), // POST不缓存
    put: (url, data) => request(url, 'PUT', data, false),
    delete: (url, data) => request(url, 'DELETE', data, false),
    BASE_URL,
    // 清除缓存
    clearCache: (url) => {
        try {
            const keys = wx.getStorageInfoSync().keys;
            keys.forEach(key => {
                if (key.startsWith('cache_') && (!url || key.includes(url))) {
                    wx.removeStorageSync(key);
                }
            });
        } catch (e) {
            console.error('清除缓存失败:', e);
        }
    }
};

