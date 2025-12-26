const request = require('../../utils/request.js');
const app = getApp();

Page({
    data: {
        isRegister: false, // true: 注册模式, false: 登录模式
        phone: '',
        name: '',
        password: '',
        loading: false
    },

    onLoad(options) {
        if (options.mode === 'register') {
            this.setData({ isRegister: true });
        }
    },

    // 统一输入处理
    handleInput(e) {
        const field = e.currentTarget.dataset.field;
        this.setData({ [field]: e.detail.value.trim() });
    },

    // 切换模式
    switchMode() {
        this.setData({
            isRegister: !this.data.isRegister,
            // 切换时清空敏感信息，保留手机号方便用户
            password: '',
            name: ''
        });
    },

    // 核心验证逻辑
    validate() {
        const { phone, password, name, isRegister } = this.data;

        if (!phone) {
            wx.showToast({ title: '请输入手机号', icon: 'none' });
            return false;
        }
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            wx.showToast({ title: '手机号格式有误', icon: 'none' });
            return false;
        }
        if (!password || password.length < 6) {
            wx.showToast({ title: '密码至少6位', icon: 'none' });
            return false;
        }
        if (isRegister && !name) {
            wx.showToast({ title: '请输入姓名', icon: 'none' });
            return false;
        }
        return true;
    },

    // 登录
    async handleLogin() {
        if (!this.validate()) return;

        this.setData({ loading: true });

        try {
            // 这里的 API 路径需与您的后端对应
            const res = await request.post('/auth/login', {
                phone: this.data.phone,
                password: this.data.password
            });

            if (res && res.code === 200) {
                const userData = res.data;

                // 1. 存储 Token
                wx.setStorageSync('token', userData.token);
                wx.setStorageSync('userId', userData.userId);

                // 2. 存储用户信息
                const userInfo = {
                    nickName: userData.name || this.data.phone,
                    avatarUrl: userData.avatar || '/assets/images/user-active.png',
                    phone: userData.phone,
                    level: 1 // 默认等级
                };
                wx.setStorageSync('userInfo', userInfo);

                // 3. 更新全局变量
                app.globalData.isLogin = true;
                app.globalData.userInfo = userInfo;

                wx.showToast({ title: '登录成功', icon: 'success' });

                // 4. 智能跳转
                setTimeout(() => {
                    const pages = getCurrentPages();
                    if (pages.length > 1) {
                        wx.navigateBack(); // 返回上一页
                    } else {
                        wx.switchTab({ url: '/pages/mine/mine' }); // 去个人中心
                    }
                }, 1000);

            } else {
                throw new Error(res.message || '登录失败');
            }
        } catch (err) {
            console.error('Login Error:', err);
            wx.showToast({
                title: err.message || '网络异常，请重试',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    // 注册
    async handleRegister() {
        if (!this.validate()) return;

        this.setData({ loading: true });

        try {
            const res = await request.post('/auth/register', {
                phone: this.data.phone,
                password: this.data.password,
                name: this.data.name
            });

            if (res && res.code === 200) {
                wx.showToast({ title: '注册成功', icon: 'success' });

                // 注册成功后，延迟自动登录或切换到登录视图
                setTimeout(() => {
                    this.setData({ isRegister: false });
                    // 可选：直接调用登录
                    this.handleLogin();
                }, 1000);
            } else {
                throw new Error(res.message || '注册失败');
            }
        } catch (err) {
            console.error('Register Error:', err);
            wx.showToast({
                title: err.message || '注册失败，该手机号可能已存在',
                icon: 'none'
            });
            this.setData({ loading: false });
        }
    }
});