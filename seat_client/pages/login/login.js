const request = require('../../utils/request.js');

Page({
    data: {
        isRegister: false, // 是否是注册模式
        phone: '',
        name: '',
        code: '',
        codeText: '获取验证码',
        countdown: 0,
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
            code: ''
        });
    },

    // 发送验证码
    async sendCode() {
        const { phone, countdown } = this.data;
        
        if (!phone) {
            return wx.showToast({ title: '请输入手机号', icon: 'none' });
        }

        // 验证手机号格式
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
        }

        // 倒计时中，不允许重复发送
        if (countdown > 0) {
            return;
        }

        try {
            // 发送验证码接口使用 GET 方法，phone 作为 query 参数
            const res = await request.get('/api/auth/send-code', { phone });
            
            if (res && res.code === 200) {
                wx.showToast({ title: '验证码已发送', icon: 'success' });
                
                // 开始倒计时
                let count = 60;
                this.setData({ countdown: count, codeText: `${count}秒后重发` });
                
                const timer = setInterval(() => {
                    count--;
                    if (count > 0) {
                        this.setData({ countdown: count, codeText: `${count}秒后重发` });
                    } else {
                        this.setData({ countdown: 0, codeText: '获取验证码' });
                        clearInterval(timer);
                    }
                }, 1000);
            }
        } catch (err) {
            console.error('发送验证码失败', err);
        }
    },

    // 登录逻辑
    async handleLogin() {
        const { phone, code } = this.data;

        if (!phone) {
            return wx.showToast({ title: '请输入手机号', icon: 'none' });
        }

        if (!code) {
            return wx.showToast({ title: '请输入验证码', icon: 'none' });
        }

        // 验证手机号格式
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
        }

        this.setData({ loading: true });

        try {
            const res = await request.post('/api/auth/login', { phone, code });

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
                throw new Error(res.msg || '登录失败');
            }
        } catch (err) {
            console.error('登录失败', err);
        } finally {
            this.setData({ loading: false });
        }
    },

    // 注册逻辑
    async handleRegister() {
        const { phone, name, code } = this.data;

        if (!phone) {
            return wx.showToast({ title: '请输入手机号', icon: 'none' });
        }

        if (!name) {
            return wx.showToast({ title: '请输入姓名', icon: 'none' });
        }

        if (!code) {
            return wx.showToast({ title: '请输入验证码', icon: 'none' });
        }

        // 验证手机号格式
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
        }

        this.setData({ loading: true });

        try {
            const res = await request.post('/api/auth/register', { phone, name, code });

            if (res && res.code === 200) {
                wx.showToast({ title: '注册成功，请登录', icon: 'success' });
                
                // 切换到登录模式
                setTimeout(() => {
                    this.setData({ isRegister: false, name: '', code: '' });
                }, 1500);
            }
        } catch (err) {
            console.error('注册失败', err);
        } finally {
            this.setData({ loading: false });
        }
    }
});