import request from '../../utils/request.js';

Page({
    data: {
        studentId: '',
        password: '',
        loading: false
    },

    // 输入处理
    handleInput(e) {
        const field = e.currentTarget.dataset.field;
        this.setData({ [field]: e.detail.value });
    },

    // 登录逻辑
    async handleLogin() {
        const { studentId, password } = this.data;

        if (!studentId || !password) {
            return wx.showToast({ title: '请输入学号和密码', icon: 'none' });
        }

        this.setData({ loading: true });

        try {
            // 调用后端 /api/auth/login 接口
            // 这里不使用 api/auth.js 封装，直接用 request 演示最直观逻辑
            // 注意：request.js 封装里 url 前面会自动拼上 BASE_URL
            const res = await request('/auth/login', 'POST', {
                studentId: studentId,
                password: password
            }, false); // false 表示这个请求不需要 token

            // 登录成功，保存 Token 和用户信息
            wx.setStorageSync('token', res.token);
            wx.setStorageSync('userId', res.userId);
            wx.setStorageSync('userName', res.name);

            wx.showToast({ title: '登录成功', icon: 'success' });

            // 跳转到首页
            setTimeout(() => {
                wx.switchTab({ url: '/pages/index/index' });
            }, 1000);

        } catch (err) {
            console.error(err);
            // 错误提示已经在 request.js 中处理了(wx.showToast)，这里可以不写
        } finally {
            this.setData({ loading: false });
        }
    },

    // 简单的注册逻辑（复用登录页或者跳新页，这里做个简单演示）
    async handleRegister() {
        const { studentId, password } = this.data;
        if (!studentId || !password) {
            return wx.showToast({ title: '注册需填学号密码', icon: 'none' });
        }

        // 简单的注册交互
        wx.showModal({
            title: '注册确认',
            content: `确定注册学号 ${studentId} 吗？`,
            success: async (res) => {
                if (res.confirm) {
                    try {
                        await request('/auth/register', 'POST', {
                            studentId,
                            password,
                            name: '同学' + studentId.substr(-4) // 默认昵称
                        }, false);
                        wx.showToast({ title: '注册成功，请登录' });
                    } catch(e) {}
                }
            }
        });
    }
});