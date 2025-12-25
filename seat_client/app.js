const authApi = require('./api/auth.js');

App({
    globalData: {
        userInfo: null,
        isLogin: false
    },

    onLaunch() {
        // 小程序启动时，检查登录状态
        this.checkLoginStatus();
    },

    checkLoginStatus() {
        const token = wx.getStorageSync('token');
        const userInfo = wx.getStorageSync('userInfo');
        
        if (token && userInfo) {
            // 有Token和用户信息，视为已登录
            this.globalData.isLogin = true;
            this.globalData.userInfo = userInfo;
            console.log('已登录，用户信息已加载');
        } else {
            // 未登录，跳转到登录页
            this.globalData.isLogin = false;
            console.log('未登录，需要跳转到登录页');
        }
    },

    // 跳转到登录页
    navigateToLogin() {
        wx.navigateTo({
            url: '/pages/login/login'
        });
    }
});