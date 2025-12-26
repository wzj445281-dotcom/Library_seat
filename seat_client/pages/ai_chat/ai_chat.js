const app = getApp();
const request = require('../../utils/request.js');

Page({
    data: {
        messageList: [],      // 聊天记录
        inputValue: '',       // 输入框内容
        loading: false,       // AI思考中状态
        scrollTop: 0,         // 滚动条位置
        userInfo: null        // 用户头像信息
    },

    onLoad() {
        // 1. 获取用户信息用于显示头像 (如果有)
        const userInfo = wx.getStorageSync('userInfo');
        this.setData({ userInfo });

        // 2. AI 欢迎语
        this.addRobotMessage('您好！我是瑞幸 AI 助手 ☕️\n我可以为您推荐饮品、查询优惠，或者聊聊咖啡知识。\n试试问我："有什么好喝的推荐？"');
    },

    /**
     * 监听输入框输入
     */
    handleInput(e) {
        this.setData({ inputValue: e.detail.value });
    },

    /**
     * 发送消息
     */
    sendMessage() {
        const content = this.data.inputValue.trim();
        // 如果内容为空或正在加载中，不发送
        if (!content || this.data.loading) return;

        // 1. 立即上屏用户消息
        this.addUserMessage(content);

        // 清空输入框并显示加载状态
        this.setData({
            inputValue: '',
            loading: true
        });

        // 2. 调用后端 API
        request.post('/app/ai/chat', { message: content })
            .then(res => {
                if (res) {
                    // 兼容处理：后端可能直接返回 Map，也可能封装在 data 中
                    // 根据你的 Controller，它是直接返回 ApiResponse<Map>，所以数据在 res 中 (request.js已解包)
                    const reply = res.reply || 'AI 暂时没话说了...';
                    const recommendations = res.recommendations || [];

                    this.addRobotMessage(reply, recommendations);
                } else {
                    this.addRobotMessage('抱歉，我好像走神了，请再问一次 🤯');
                }
            })
            .catch(err => {
                console.error('AI Chat Error:', err);
                this.addRobotMessage('网络连接似乎出了点问题，请检查网络 📶');
            })
            .finally(() => {
                this.setData({ loading: false });
            });
    },

    /**
     * 添加用户消息到列表
     */
    addUserMessage(content) {
        const msg = { type: 'user', content: content };
        this.setData({
            messageList: [...this.data.messageList, msg]
        }, () => {
            this.scrollToBottom();
        });
    },

    /**
     * 添加机器人消息到列表
     */
    addRobotMessage(content, recommendations = []) {
        const msg = {
            type: 'robot',
            content: content,
            recommendations: recommendations
        };
        this.setData({
            messageList: [...this.data.messageList, msg]
        }, () => {
            this.scrollToBottom();
        });
    },

    /**
     * 滚动到底部
     */
    scrollToBottom() {
        this.setData({
            scrollTop: this.data.messageList.length * 1000 // 简单粗暴的滚动到底部
        });
    },

    /**
     * 点击推荐商品，跳转到菜单页
     */
    onRecommendTap(e) {
        const pid = e.currentTarget.dataset.pid;
        // 跳转到菜单页面 (TabBar页面需使用 switchTab)
        wx.switchTab({
            url: '/pages/menu/index',
            success: () => {
                // 可选：这里可以存一下 pid，在菜单页 onLoad 里自动定位到该商品
                wx.setStorageSync('targetProductId', pid);
            }
        });
    }
});