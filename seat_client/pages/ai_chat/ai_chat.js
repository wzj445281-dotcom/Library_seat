const app = getApp();
const request = require('../../utils/request.js');

Page({
    data: {
        inputValue: '',
        messageList: [],
        scrollTop: 0,
        loading: false,
        isFocus: false
    },

    onLoad: function(options) {
        this.addMessage('ai', '您好！我是瑞幸智能助手 ☕️\n我可以为您推荐饮品、查询门店信息，快来问我吧！');
    },

    onInput: function(e) {
        this.setData({
            inputValue: e.detail.value
        });
    },

    sendMessage: function() {
        var content = this.data.inputValue.trim();
        if (!content) return;

        this.addMessage('user', content);
        this.setData({
            inputValue: '',
            loading: true
        });

        console.log('发送消息:', content);
        
        var self = this;
        request.post('/app/ai/chat', { message: content })
            .then(function(res) {
                console.log('AI响应:', res);
                var aiData = res.data;
                self.addMessage('ai', aiData.reply, aiData.recommendations);
            })
            .catch(function(err) {
                console.error('API Error:', err);
                self.addMessage('ai', '抱歉，我现在有点累，请稍后再试 😵‍💫');
            })
            .finally(function() {
                self.setData({ loading: false });
            });
    },

    addMessage: function(role, content, recommendations) {
        if (recommendations === undefined) {
            recommendations = [];
        }
        
        var list = this.data.messageList;
        list.push({
            role: role,
            content: content,
            recommendations: recommendations
        });

        this.setData({
            messageList: list,
            scrollTop: list.length * 1000
        });
    },

    onProductClick: function(e) {
        var product = e.currentTarget.dataset.item;
        wx.showToast({
            title: '已选择: ' + product.name,
            icon: 'none'
        });
    },

    onTagClick: function(e) {
        var text = e.currentTarget.dataset.text;
        var self = this;
        this.setData({ inputValue: text }, function() {
            self.sendMessage();
        });
    }
});