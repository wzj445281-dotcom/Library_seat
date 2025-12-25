const request = require('../../utils/request.js');

Page({
    data: {
        content: '',
        contact: '',
        loading: false
    },

    handleSubmit() {
        if (!this.data.content) {
            return wx.showToast({ title: '请填写内容', icon: 'none' });
        }

        this.setData({ loading: true });

        request.post('/api/app/feedback/submit', {
            content: this.data.content,
            contact: this.data.contact
        }).then(res => {
            if (res.code === 200) {
                wx.showToast({ title: '提交成功', icon: 'success' });
                // 延迟返回上一页
                setTimeout(() => {
                    wx.navigateBack();
                }, 1500);
            } else {
                wx.showToast({ title: res.message || '提交失败', icon: 'none' });
            }
        }).catch(err => {
            console.error('提交反馈失败', err);
            wx.showToast({ title: '网络异常', icon: 'none' });
        }).finally(() => {
            this.setData({ loading: false });
        });
    }
});