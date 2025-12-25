/**
 * API请求工具 - 本地请求，不需要网络
 * 所有请求都使用相对路径，通过SpringBoot后端处理
 */

const API_BASE = '/api';

// 请求函数
function request(url, method, data) {
    return new Promise((resolve, reject) => {
        const token = localStorage.getItem('token');
        
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        
        const options = {
            method: method,
            headers: headers
        };
        
        if (method !== 'GET' && data) {
            options.body = JSON.stringify(data);
        }
        
        let requestUrl = API_BASE + url;
        if (method === 'GET' && data) {
            const queryString = Object.keys(data)
                .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
                .join('&');
            if (queryString) {
                requestUrl += (url.includes('?') ? '&' : '?') + queryString;
            }
        }
        
        fetch(requestUrl, options)
            .then(response => {
                if (response.status === 200) {
                    return response.json();
                } else if (response.status === 401) {
                    localStorage.removeItem('token');
                    window.location.href = '/login';
                    reject(new Error('未登录'));
                } else {
                    return response.json().then(data => {
                        throw new Error(data.message || '请求失败');
                    });
                }
            })
            .then(data => {
                if (data.code === 200) {
                    resolve(data);
                } else {
                    alert(data.message || '操作失败');
                    reject(data);
                }
            })
            .catch(err => {
                console.error('请求失败:', err);
                alert('网络错误: ' + err.message);
                reject(err);
            });
    });
}

// 导出API方法
const api = {
    get: (url, data) => request(url, 'GET', data),
    post: (url, data) => request(url, 'POST', data),
    put: (url, data) => request(url, 'PUT', data),
    delete: (url, data) => request(url, 'DELETE', data)
};

// 全局暴露
window.api = api;

