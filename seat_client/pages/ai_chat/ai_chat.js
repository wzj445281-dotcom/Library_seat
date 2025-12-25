const request = require('../../utils/request.js');
const app = getApp();

Page({
    data: {
        messageList: [
            {
                type: 'robot',
                content: '您好！我是瑞幸 AI 助手 ☕️，可以帮您推荐商品、解答问题。试试问我"推荐"或"什么好喝"吧！',
                recommendations: []
            }
        ],
        inputValue: '',
        loading: false,
        scrollTop: 0,
        userInfo: null
    },

    onLoad: function (options) {
        // 设置导航栏标题
        wx.setNavigationBarTitle({
            title: 'AI 助手'
        });
        
        // 获取用户信息
        const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo') || {};
        this.setData({
            userInfo: userInfo
        });
    },

    handleInput(e) {
        this.setData({
            inputValue: e.detail.value
        });
    },

    sendMessage() {
        const content = this.data.inputValue.trim();
        if (!content) return;

        // 1. 立即显示用户消息
        const userMsg = { type: 'user', content: content, recommendations: [] };
        const newList = [...this.data.messageList, userMsg];

        this.setData({
            messageList: newList,
            inputValue: '',
            loading: true,
            scrollTop: newList.length * 1000 // 滚动到底部
        });

        // 2. 请求后端接口
        request.post('/api/app/ai/chat', { message: content })
            .then(res => {
                let replyContent = '系统繁忙，请稍后再试';
                let recommendations = [];
                
                if (res.code === 200 || res.code === 0) {
                    const data = res.data || {};
                    replyContent = data.reply || '抱歉，我暂时无法理解您的问题。';
                    recommendations = data.recommendations || [];
                } else {
                    replyContent = res.msg || '出错了';
                }

                const robotMsg = { 
                    type: 'robot', 
                    content: replyContent,
                    recommendations: recommendations
                };
                const updatedList = [...this.data.messageList, robotMsg];

                this.setData({
                    messageList: updatedList,
                    loading: false,
                    scrollTop: updatedList.length * 1000
                });
            })
            .catch(err => {
                console.error('AI 请求失败:', err);
                this.setData({
                    loading: false,
                    messageList: [...this.data.messageList, { 
                        type: 'robot', 
                        content: '网络连接失败，请检查网络。',
                        recommendations: []
                    }]
                });
            });
    },

    // 点击推荐商品，跳转到商品详情或加入购物车
    onRecommendTap(e) {
        const pid = e.currentTarget.dataset.pid;
        if (pid) {
            // 跳转到商品详情页（如果存在）或菜单页
            wx.navigateTo({
                url: `/pages/menu/index?productId=${pid}`
            });
        }
    }
});