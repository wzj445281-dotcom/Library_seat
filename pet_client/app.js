// app.js
App({
    onLaunch() {
        // 1. 获取系统信息（用于适配刘海屏等）
        try {
            const systemInfo = wx.getSystemInfoSync();
            this.globalData.systemInfo = systemInfo;
        } catch (e) {
            console.error('获取系统信息失败', e);
        }

        // 2. 登录（获取 code，后续在 login 页面或 request 中换取 token）
        wx.login({
            success: res => {
                // 发送 res.code 到后台换取 openId, sessionKey, unionId
                // 注意：实际的 token 换取逻辑通常在 login 页面或 auth.js 中完成
                // 这里仅打印 code 证明登录能力正常初始化
                console.log('App Launch Login Code:', res.code);
            }
        });

        // 3. 检查小程序版本更新
        this.checkUpdate();
    },

    /**
     * 检测并应用新版本
     */
    checkUpdate() {
        if (wx.canIUse('getUpdateManager')) {
            const updateManager = wx.getUpdateManager();

            updateManager.onCheckForUpdate(function (res) {
                // 请求完新版本信息的回调
                if (res.hasUpdate) {
                    console.log('检测到新版本');
                }
            });

            updateManager.onUpdateReady(function () {
                wx.showModal({
                    title: '更新提示',
                    content: '新版本已经准备好，是否重启应用？',
                    success: function (res) {
                        if (res.confirm) {
                            // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                            updateManager.applyUpdate();
                        }
                    }
                });
            });

            updateManager.onUpdateFailed(function () {
                // 新的版本下载失败
                wx.showModal({
                    title: '已经有新版本了',
                    content: '新版本已经上线啦~，请您删除当前小程序，重新搜索打开哟~'
                });
            });
        }
    },

    // 全局数据
    globalData: {
        userInfo: null,      // 用户信息
        token: null,         // 登录令牌
        isLogin: false,      // 登录状态
        systemInfo: null,    // 系统设备信息
        // 如果 config.js 还没有引入，可以在这里暂存 baseUrl，
        // 但建议在 utils/request.js 中统一管理 API 地址
        baseUrl: 'http://localhost:8080/api'
    }
})