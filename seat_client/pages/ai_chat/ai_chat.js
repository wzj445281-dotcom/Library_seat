import request from '../../utils/request.js';

Page({
    data: {
        inputVal: '',
        messages: [
            { type: 'ai', content: '您好！我是智座AI小助手。想喝点什么？或者有什么我可以帮您的？' }
        ],
        quickQuestions: ['推荐新品', '我的订单在哪', '怎么联系客服', '门店营业时间'],
        loading: false,
        scrollTop: 0,
        isRecording: false
    },

    onLoad() {
        // 初始化录音管理器
        this.recorderManager = wx.getRecorderManager();
        this.recorderManager.onStop((res) => {
            console.log('录音文件', res.tempFilePath);
            // 模拟语音转文字
            this.mockVoiceToText(res.tempFilePath);
        });
        this.recorderManager.onError((err) => {
            console.error('录音失败', err);
            wx.showToast({ title: '录音失败，请重试', icon: 'none' });
            this.setData({ isRecording: false });
        });
    },

    // 语音输入处理
    handleVoiceStart() {
        this.setData({ isRecording: true });
        this.recorderManager.start({ format: 'mp3' });
        wx.showToast({ title: '正在聆听...', icon: 'loading', duration: 60000 });
    },

    handleVoiceEnd() {
        this.setData({ isRecording: false });
        this.recorderManager.stop();
        wx.hideToast();
    },

    mockVoiceToText(filePath) {
        // 模拟识别延迟
        wx.showLoading({ title: '识别中...' });
        setTimeout(() => {
            wx.hideLoading();
            const mockTexts = ['我想喝生椰拿铁', '查看我的积分', '哪里有空座位'];
            const randomText = mockTexts[Math.floor(Math.random() * mockTexts.length)];
            this.setData({ inputVal: randomText });
            this.handleSend(); // 自动发送
        }, 800);
    },

    handleInput(e) {
        this.setData({ inputVal: e.detail.value });
    },

    // 快捷回复点击
    handleQuickTap(e) {
        const text = e.currentTarget.dataset.text;
        this.setData({ inputVal: text });
        this.handleSend();
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
            scrollTop: (this.data.messages.length + 1) * 1000
        });

        try {
            // 2. 模拟调用后端 AI 接口 (或者使用真实的 request)
            // const res = await request('/app/ai/chat', 'POST', { message: content });

            // 模拟回复
            let reply = '这就为您查找相关信息...';
            if (content.includes('推荐')) reply = '强烈推荐我们新出的【生酪拿铁】，口感丝滑！';
            else if (content.includes('订单')) reply = '您可以在“订单”页面查看您的历史消费记录。';
            else if (content.includes('客服')) reply = '客服热线：400-888-8888，服务时间 9:00-18:00。';

            setTimeout(() => {
                const aiMsg = { type: 'ai', content: reply };
                this.setData({
                    messages: [...this.data.messages, aiMsg],
                    loading: false,
                    scrollTop: (this.data.messages.length + 2) * 1000
                });
            }, 1000);

        } catch (err) {
            this.setData({
                loading: false,
                messages: [...this.data.messages, { type: 'ai', content: '网络开小差了，请稍后再试。' }]
            });
        }
    }
});