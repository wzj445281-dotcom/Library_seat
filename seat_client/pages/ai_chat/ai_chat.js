const app = getApp();
// 引入封装好的请求工具 (根据您的文件结构，如果有 request.js 建议使用，这里演示原生 wx.request 以保证独立性)
// const request = require('../../utils/request.js');

Page({
    /**
     * 页面的初始数据
     */
    data: {
        inputValue: '',       // 输入框内容
        messageList: [],      // 消息列表 { role: 'user'/'ai', content: string, recommendations: [] }
        scrollTop: 0,         // 滚动条位置
        loading: false,       // 是否正在思考中
        isFocus: false        // 输入框聚焦状态
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        // 初始化欢迎语
        this.addMessage('ai', '您好！我是瑞幸智能助手 ☕️\n我可以为您推荐饮品、查询门店信息，快来问我吧！');
    },

    /**
     * 监听输入框内容变化
     */
    onInput(e) {
        this.setData({
            inputValue: e.detail.value
        });
    },

    /**
     * 发送消息主逻辑
     */
    sendMessage() {
        const content = this.data.inputValue.trim();
        if (!content) return;

        // 1. 添加用户消息到列表
        this.addMessage('user', content);

        // 清空输入框并显示加载状态
        this.setData({
            inputValue: '',
            loading: true
        });

        // 2. 调用后端 API
        // 注意：这里 URL 需要根据您的真机调试/本地环境配置
        // 如果是本地调试，通常是 http://localhost:8080/api/app/ai/chat
        // 如果使用了内网穿透或局域网 IP，请替换为对应的 IP
        const baseUrl = app.globalData.baseUrl || 'http://localhost:8080';

        wx.request({
            url: `${baseUrl}/api/app/ai/chat`,
            method: 'POST',
            data: {
                message: content
            },
            header: {
                'content-type': 'application/json',
                // 如果有 Token 鉴权，记得带上
                // 'Authorization': wx.getStorageSync('token')
            },
            success: (res) => {
                // 3. 处理成功响应
                if (res.statusCode === 200 && res.data.code === 200) {
                    const aiData = res.data.data; // 后端返回的结构: { reply: "...", recommendations: [...] }

                    this.addMessage('ai', aiData.reply, aiData.recommendations);
                } else {
                    console.error('API Error:', res);
                    this.addMessage('ai', '抱歉，我现在有点累，请稍后再试 😵‍💫');
                }
            },
            fail: (err) => {
                console.error('Network Error:', err);
                this.addMessage('ai', '网络连接失败，请检查网络设置 📶');
            },
            complete: () => {
                this.setData({ loading: false });
            }
        });
    },

    /**
     * 通用：添加消息到列表并滚动
     * @param {String} role 角色 'user' | 'ai'
     * @param {String} content 文本内容
     * @param {Array} recommendations 推荐商品数组 (可选)
     */
    addMessage(role, content, recommendations = []) {
        const list = this.data.messageList;
        list.push({
            role: role,
            content: content,
            recommendations: recommendations
        });

        this.setData({
            messageList: list,
            // 计算滚动高度，确保滚动到底部 (乘以一个足够大的系数)
            scrollTop: list.length * 1000
        });
    },

    /**
     * 点击推荐商品卡片
     */
    onProductClick(e) {
        const product = e.currentTarget.dataset.item;
        // 跳转到商品详情页，或者是直接去点单页
        // 这里假设跳转到点餐页并带上商品ID，或者弹出规格选择
        wx.showToast({
            title: `已选择: ${product.name}`,
            icon: 'none'
        });

        // 示例：跳转到菜单页
        // wx.switchTab({ url: '/pages/menu/index' });
    },

    /**
     * 点击快捷问题 (如果有)
     */
    onTagClick(e) {
        const text = e.currentTarget.dataset.text;
        this.setData({ inputValue: text }, () => {
            this.sendMessage();
        });
    }
});