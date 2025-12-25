const authApi = require('./api/auth.js');

App({
    globalData: {
        userInfo: null,
        isLogin: false
    },

    onLaunch() {
        // 小程序启动时，检查并执行登录
        this.checkLoginStatus();
    },

    checkLoginStatus() {
        const token = wx.getStorageSync('token');
        // 如果没有 Token，或者 session 过期，则重新登录
        if (!token) {
            this.doLogin();
        } else {
            // 检查微信 session 是否有效
            wx.checkSession({
                success: () => {
                    // session 有效，且有 Token，视为已登录
                    this.globalData.isLogin = true;
                    // 可选：这里可以调用 getProfile 接口刷新用户信息
                },
                fail: () => {
                    // session 过期，重新登录
                    this.doLogin();
                }
            });
        }
    },

    // 执行登录流程
    doLogin() {
        wx.login({
            success: res => {
                if (res.code) {
                    // 发送 res.code 到后台换取 openId, sessionKey, unionId
                    authApi.login(res.code).then(res => {
                        if (res.code === 200 && res.data.token) {
                            // 1. 存储 Token
                            wx.setStorageSync('token', res.data.token);
                            this.globalData.isLogin = true;

                            // 2. 存储用户信息 (如果有返回)
                            if (res.data.user) {
                                this.globalData.userInfo = res.data.user;
                                wx.setStorageSync('userInfo', res.data.user);
                            }

                            console.log('登录成功，Token已更新');
                        }
                    }).catch(err => {
                        console.error('登录失败', err);
                        // 这里可以增加重试机制
                    });
                } else {
                    console.error('微信登录失败！' + res.errMsg);
                }
            }
        });
    }
});