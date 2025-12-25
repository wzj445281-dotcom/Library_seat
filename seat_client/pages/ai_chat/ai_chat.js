const request = require('../../utils/request.js');
const app = getApp();

Page({
    data: {
        messageList: [
            {
                type: 'robot',
                content: '你好！我是您的智能选座助手。您可以问我：“哪里比较安静？” 或 “营业时间是几点？”'
            }
        ],
        inputValue: '',
        loading: false,
        scrollTop: 0
    },

    onLoad: function (options) {
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
        const userMsg = { type: 'user', content: content };
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
                // 假设您的 request.js 返回结构是 res.code 和 res.data
                // 如果直接返回数据，请调整为 res.data 或 res
                if (res.code === 200 || res.code === 0) {
                    replyContent = res.data;
                } else {
                    replyContent = res.msg || '出错了';
                }

                const robotMsg = { type: 'robot', content: replyContent };
                const updatedList = [...this.data.messageList, robotMsg];

                this.setData({
                    messageList: updatedList,
                    loading: false,
                    scrollTop: updatedList.length * 1000
                });
            })
            .catch(err => {
                console.error(err);
                this.setData({
                    loading: false,
                    messageList: [...this.data.messageList, { type: 'robot', content: '网络连接失败，请检查网络。' }]
                });
            });
    }
});