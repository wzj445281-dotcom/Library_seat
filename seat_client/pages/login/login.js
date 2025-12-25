const request = require('../../utils/request.js');

Page({
    data: {
        isRegister: false, // 是否是注册模式
        phone: '',
        name: '',
        password: '',
        loading: false
    },

    onLoad(options) {
        // 如果从其他页面跳转过来，可能是注册模式
        if (options.mode === 'register') {
            this.setData({ isRegister: true });
        }
    },

    // 输入处理
    handleInput(e) {
        const field = e.currentTarget.dataset.field;
        this.setData({ [field]: e.detail.value });
    },

    // 切换登录/注册模式
    switchMode() {
        this.setData({
            isRegister: !this.data.isRegister,
            phone: '',
            name: '',
            password: ''
        });
    },

    // 登录逻辑
    async handleLogin() {
        const { phone, password } = this.data;

        if (!phone) {
            return wx.showToast({ title: '请输入手机号', icon: 'none' });
        }

        if (!password) {
            return wx.showToast({ title: '请输入密码', icon: 'none' });
        }

        // 验证手机号格式
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
        }

        // 验证密码长度
        if (password.length < 6) {
            return wx.showToast({ title: '密码至少6位', icon: 'none' });
        }

        this.setData({ loading: true });

        try {
            const res = await request.post('/auth/login', { phone, password });

            if (res && res.code === 200 && res.data) {
                // 保存登录信息
                wx.setStorageSync('token', res.data.token);
                wx.setStorageSync('userId', res.data.userId);
                wx.setStorageSync('userName', res.data.name);
                wx.setStorageSync('userInfo', {
                    nickName: res.data.name,
                    phone: res.data.phone
                });

                // 更新全局状态
                const app = getApp();
                app.globalData.isLogin = true;
                app.globalData.userInfo = {
                    nickName: res.data.name,
                    phone: res.data.phone
                };

                wx.showToast({ title: '登录成功', icon: 'success' });

                // 跳转到首页
                setTimeout(() => {
                    wx.switchTab({ url: '/pages/menu/index' });
                }, 1000);
            } else {
                throw new Error(res.message || res.msg || '登录失败');
            }
        } catch (err) {
            console.error('登录失败', err);
            wx.showToast({
                title: err.message || '登录失败，请检查手机号和密码',
                icon: 'none',
                duration: 2000
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    // 注册逻辑
    async handleRegister() {
        const { phone, name, password } = this.data;

        if (!phone) {
            return wx.showToast({ title: '请输入手机号', icon: 'none' });
        }

        if (!name) {
            return wx.showToast({ title: '请输入姓名', icon: 'none' });
        }

        if (!password) {
            return wx.showToast({ title: '请输入密码', icon: 'none' });
        }

        // 验证手机号格式
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
        }

        // 验证密码长度
        if (password.length < 6) {
            return wx.showToast({ title: '密码至少6位', icon: 'none' });
        }

        this.setData({ loading: true });

        try {
            const res = await request.post('/auth/register', { phone, name, password });

            if (res && res.code === 200) {
                wx.showToast({ title: '注册成功，请登录', icon: 'success' });
                
                // 切换到登录模式
                setTimeout(() => {
                    this.setData({ isRegister: false, name: '', password: '' });
                }, 1500);
            } else {
                throw new Error(res.message || res.msg || '注册失败');
            }
        } catch (err) {
            console.error('注册失败', err);
            wx.showToast({
                title: err.message || '注册失败，请重试',
                icon: 'none',
                duration: 2000
            });
        } finally {
            this.setData({ loading: false });
        }
    }
});