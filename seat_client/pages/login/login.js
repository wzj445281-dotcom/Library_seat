const request = require('../../utils/request.js');
const authApi = require('../../api/auth.js');

Page({
    data: {
        isRegister: false, // 是否是注册模式
        phone: '',
        name: '',
        password: '',
        loading: false,
        // 账号密码登录/注册相关
        isAccountRegister: false, // 是否是账号注册模式
        accountUsername: '',
        accountNickname: '',
        accountPassword: '',
        accountLoading: false
    },

    onLoad(options) {
        // 如果从其他页面跳转过来，可能是注册模式
        if (options.mode === 'register') {
            this.setData({ isRegister: true });
        }
        
        // 检查是否有保存的登录信息，自动填充
        this.loadSavedLoginInfo();
    },

    /**
     * 加载保存的登录信息（记住登录功能）
     */
    loadSavedLoginInfo() {
        try {
            // 检查是否有保存的账号密码
            const savedUsername = wx.getStorageSync('savedUsername');
            const savedPassword = wx.getStorageSync('savedPassword');
            
            if (savedUsername && savedPassword) {
                // 自动填充账号密码
                this.setData({
                    accountUsername: savedUsername,
                    accountPassword: savedPassword
                });
                
                // 提示用户可以直接点击登录
                console.log('已自动填充上次登录的账号密码');
            }
        } catch (e) {
            console.error('加载保存的登录信息失败', e);
        }
    },

    /**
     * 演示模式登录 - 答辩专用，绕过微信验证
     */
    async handleMockLogin() {
        wx.showLoading({ title: '演示模式登录中...' });
        try {
            const res = await authApi.mockLogin();
            if (res && res.code === 200 && res.data) {
                // 保存登录信息
                wx.setStorageSync('token', res.data.token);
                wx.setStorageSync('userId', res.data.userId);
                wx.setStorageSync('userName', res.data.name);
                wx.setStorageSync('userInfo', {
                    nickName: res.data.name || '演示用户',
                    phone: res.data.phone || ''
                });

                // 更新全局状态
                const app = getApp();
                app.globalData.isLogin = true;
                app.globalData.token = res.data.token;
                app.globalData.userInfo = {
                    nickName: res.data.name || '演示用户',
                    phone: res.data.phone || ''
                };

                wx.hideLoading();
                wx.showToast({
                    title: '演示模式已开启',
                    icon: 'success',
                    duration: 2000
                });

                // 跳转到首页
                setTimeout(() => {
                    wx.switchTab({ url: '/pages/menu/index' });
                }, 1000);
            } else {
                throw new Error(res.message || res.msg || '演示登录失败');
            }
        } catch (err) {
            console.error('演示登录失败', err);
            wx.hideLoading();
            wx.showToast({
                title: err.message || '演示登录失败，请检查后端服务',
                icon: 'none',
                duration: 2000
            });
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
    },

    // 切换账号密码登录/注册模式
    switchAccountMode() {
        this.setData({
            isAccountRegister: !this.data.isAccountRegister,
            accountUsername: '',
            accountNickname: '',
            accountPassword: ''
        });
    },

    // 账号密码登录逻辑
    async handleAccountLogin() {
        const { accountUsername, accountPassword } = this.data;

        if (!accountUsername) {
            return wx.showToast({ title: '请输入用户名', icon: 'none' });
        }

        if (!accountPassword) {
            return wx.showToast({ title: '请输入密码', icon: 'none' });
        }

        // 验证用户名长度
        if (accountUsername.length < 3 || accountUsername.length > 20) {
            return wx.showToast({ title: '用户名长度必须在3-20个字符之间', icon: 'none' });
        }

        // 验证密码长度
        if (accountPassword.length < 6) {
            return wx.showToast({ title: '密码至少6位', icon: 'none' });
        }

        this.setData({ accountLoading: true });

        try {
            const res = await authApi.accountLogin({
                username: accountUsername,
                password: accountPassword
            });

            if (res && res.code === 200 && res.data) {
                // 保存登录信息
                wx.setStorageSync('token', res.data.token);
                wx.setStorageSync('userId', res.data.userId);
                wx.setStorageSync('userName', res.data.name);
                wx.setStorageSync('userInfo', {
                    nickName: res.data.name,
                    username: res.data.username,
                    phone: res.data.phone || ''
                });

                // 记住登录：保存账号密码（下次自动填充）
                wx.setStorageSync('savedUsername', accountUsername);
                wx.setStorageSync('savedPassword', accountPassword);

                // 更新全局状态
                const app = getApp();
                app.globalData.isLogin = true;
                app.globalData.token = res.data.token;
                app.globalData.userInfo = {
                    nickName: res.data.name,
                    username: res.data.username,
                    phone: res.data.phone || ''
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
            console.error('账号登录失败', err);
            wx.showToast({
                title: err.message || '登录失败，请检查用户名和密码',
                icon: 'none',
                duration: 2000
            });
        } finally {
            this.setData({ accountLoading: false });
        }
    },

    // 账号密码注册逻辑
    async handleAccountRegister() {
        const { accountUsername, accountNickname, accountPassword } = this.data;

        if (!accountUsername) {
            return wx.showToast({ title: '请输入用户名', icon: 'none' });
        }

        if (!accountNickname) {
            return wx.showToast({ title: '请输入昵称', icon: 'none' });
        }

        if (!accountPassword) {
            return wx.showToast({ title: '请输入密码', icon: 'none' });
        }

        // 验证用户名长度
        if (accountUsername.length < 3 || accountUsername.length > 20) {
            return wx.showToast({ title: '用户名长度必须在3-20个字符之间', icon: 'none' });
        }

        // 验证密码长度
        if (accountPassword.length < 6) {
            return wx.showToast({ title: '密码至少6位', icon: 'none' });
        }

        this.setData({ accountLoading: true });

        try {
            const res = await authApi.accountRegister({
                username: accountUsername,
                password: accountPassword,
                nickname: accountNickname
            });

            if (res && res.code === 200) {
                wx.showToast({ title: '注册成功，请登录', icon: 'success' });
                
                // 切换到登录模式
                setTimeout(() => {
                    this.setData({ 
                        isAccountRegister: false, 
                        accountNickname: '', 
                        accountPassword: '' 
                    });
                }, 1500);
            } else {
                throw new Error(res.message || res.msg || '注册失败');
            }
        } catch (err) {
            console.error('账号注册失败', err);
            wx.showToast({
                title: err.message || '注册失败，请重试',
                icon: 'none',
                duration: 2000
            });
        } finally {
            this.setData({ accountLoading: false });
        }
    }
});