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
            // 注意：request.js 的 post 方法会自动拼上 BASE_URL，所以这里只需要 /auth/login
            const res = await request.post('/auth/login', {
                studentId: studentId,
                password: password
            });

            // 登录成功，保存 Token 和用户信息
            // 后端返回结构：{ code: 200, data: { token, userId, name } }
            if (res && res.code === 200 && res.data) {
                wx.setStorageSync('token', res.data.token);
                wx.setStorageSync('userId', res.data.userId);
                wx.setStorageSync('userName', res.data.name);
                wx.setStorageSync('userInfo', {
                    nickName: res.data.name,
                    studentId: studentId
                });
            } else {
                throw new Error('登录失败：响应数据格式错误');
            }

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
                        const registerRes = await request.post('/auth/register', {
                            studentId,
                            password,
                            name: '同学' + studentId.substr(-4) // 默认昵称
                        });
                        if (registerRes && registerRes.code === 200) {
                            wx.showToast({ title: '注册成功，请登录', icon: 'success' });
                        }
                    } catch(e) {
                        console.error('注册失败', e);
                    }
                }
            }
        });
    }
});