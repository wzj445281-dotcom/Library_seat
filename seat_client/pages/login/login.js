const request = require('../../utils/request.js');
const app = getApp();

Page({
    data: {
        isRegister: false, // false:登录, true:注册
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

    // 输入处理
    handleInput(e) {
        const field = e.currentTarget.dataset.field;
        this.setData({ [field]: e.detail.value.trim() });
    },

    // 切换模式
    switchMode() {
        this.setData({
            isRegister: !this.data.isRegister,
            // 切换时不清空手机号，方便用户
            password: '',
            name: ''
        });
    },

    // 校验逻辑
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
            wx.showToast({ title: '请输入昵称', icon: 'none' });
            return false;
        }
        return true;
    },

    // 登录
    async handleLogin() {
        if (!this.validate()) return;

        this.setData({ loading: true });

        try {
            const res = await request.post('/auth/login', {
                phone: this.data.phone,
                password: this.data.password
            });

            if (res && res.code === 200) {
                this.loginSuccess(res.data);
            } else {
                throw new Error(res.message || res.msg || '登录失败');
            }
        } catch (err) {
            console.error('登录异常', err);
            wx.showToast({
                title: err.message || '账号或密码错误',
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

                // 注册成功后，自动切回登录模式，保留手机号
                setTimeout(() => {
                    this.setData({
                        isRegister: false,
                        password: '' // 清空密码让用户重新输入以确认
                    });
                }, 1500);
            } else {
                throw new Error(res.message || res.msg || '注册失败');
            }
        } catch (err) {
            wx.showToast({
                title: err.message || '该手机号可能已注册',
                icon: 'none'
            });
        } finally {
            this.setData({ loading: false });
        }
    },

    // 登录成功后的处理
    loginSuccess(data) {
        // 1. 存储关键信息
        wx.setStorageSync('token', data.token);
        wx.setStorageSync('userId', data.userId);

        // 2. 构造并存储用户信息
        const userInfo = {
            nickName: data.name || this.data.phone,
            phone: data.phone,
            avatarUrl: data.avatar || '/assets/images/user-active.png',
            level: 1
        };
        wx.setStorageSync('userInfo', userInfo);

        // 3. 更新全局 App 数据
        if (app.globalData) {
            app.globalData.isLogin = true;
            app.globalData.userInfo = userInfo;
        }

        wx.showToast({ title: '登录成功', icon: 'success' });

        // 4. 智能跳转：如果有上一页则返回，否则去首页
        setTimeout(() => {
            const pages = getCurrentPages();
            if (pages.length > 1) {
                wx.navigateBack();
            } else {
                wx.switchTab({ url: '/pages/menu/index' });
            }
        }, 1000);
    }
});