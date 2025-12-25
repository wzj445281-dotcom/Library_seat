const app = getApp();
const request = require('../../utils/request.js');

Page({
    data: {
        userInfo: {},
        scrollTop: 0,
        inputValue: '',
        isLoading: false,
        chatList: [
            {
                role: 'ai',
                content: '👋 你好呀！我是你的瑞幸专属 AI 咖啡师。\n\n我可以帮你：\n1. 推荐当季新品 🥤\n2. 查询咖啡热量 🔥\n3. 解决订单问题 📦\n\n想喝点什么？告诉我你的口味偏好吧~',
                recommendations: []
            }
        ]
    },

    onLoad() {
        // 获取用户信息用于显示头像
        const userInfo = wx.getStorageSync('userInfo');
        this.setData({ userInfo });
    },

    handleInput(e) {
        this.setData({ inputValue: e.detail.value });
    },

    // 发送消息
    sendMessage() {
        const content = this.data.inputValue.trim();
        if (!content || this.data.isLoading) return;

        // 1. UI 立即上屏用户消息
        const newChatList = [...this.data.chatList, { role: 'user', content }];
        this.setData({
            chatList: newChatList,
            inputValue: '',
            isLoading: true,
            scrollTop: newChatList.length * 1000 // 自动滚动到底部
        });

        // 2. 调用后端 API
        request.post('/app/ai/chat', { message: content })
            .then(res => {
                if (res.code === 200) {
                    const aiData = res.data; // { reply: "...", recommendations: [...] }

                    const aiMsg = {
                        role: 'ai',
                        content: aiData.reply,
                        recommendations: aiData.recommendations || []
                    };

                    this.setData({
                        chatList: [...this.data.chatList, aiMsg],
                        scrollTop: (this.data.chatList.length + 1) * 1000
                    });
                } else {
                    this.showErrorMsg(res.message);
                }
            })
            .catch(err => {
                console.error(err);
                this.showErrorMsg('网络连接超时，请检查网络');
            })
            .finally(() => {
                this.setData({ isLoading: false });
            });
    },

    showErrorMsg(msg) {
        this.setData({
            chatList: [...this.data.chatList, { role: 'ai', content: `(T_T) ${msg || 'AI 暂时掉线了'}` }]
        });
    },

    // 点击推荐商品卡片，跳转商品详情或去点单
    goToProduct(e) {
        const id = e.currentTarget.dataset.id;
        // 简单起见，跳转到菜单页
        wx.switchTab({
            url: '/pages/menu/index'
        });
    }
});