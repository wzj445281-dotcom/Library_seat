import request from '../../utils/request.js';

Page({
    data: {
        inputVal: '',
        messages: [
            { type: 'ai', content: '您好！我是智座AI小助手。有关信用分、预约规则的问题都可以问我哦~' }
        ],
        loading: false,
        scrollTop: 0
    },

    handleInput(e) {
        this.setData({ inputVal: e.detail.value });
    },

    async handleSend() {
        const content = this.data.inputVal;
        if (!content.trim()) return;

        // 1. 添加用户消息
        const newMsg = { type: 'user', content: content };
        this.setData({
            messages: [...this.data.messages, newMsg],
            inputVal: '',
            loading: true,
            scrollTop: this.data.messages.length * 1000 // 自动滚动到底部
        });

        try {
            // 2. 调用后端 AI 接口
            const res = await request('/app/ai/chat', 'POST', { message: content });

            // 3. 添加 AI 回复
            const aiMsg = { type: 'ai', content: res }; // res 是后端返回的 String
            this.setData({
                messages: [...this.data.messages, aiMsg],
                loading: false,
                scrollTop: (this.data.messages.length + 1) * 1000
            });
        } catch (err) {
            this.setData({
                loading: false,
                messages: [...this.data.messages, { type: 'ai', content: '网络开小差了，请稍后再试。' }]
            });
        }
    }
});