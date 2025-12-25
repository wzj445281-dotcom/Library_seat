import request from '../../utils/request.js';

Page({
    data: {
        content: '',
        contact: '',
        loading: false
    },

    async handleSubmit() {
        if (!this.data.content) {
            return wx.showToast({ title: '请填写内容', icon: 'none' });
        }

        this.setData({ loading: true });

        try {
            await request('/app/feedback/submit', 'POST', {
                content: this.data.content,
                contact: this.data.contact
            });

            wx.showToast({ title: '提交成功' });

            // 延迟返回上一页
            setTimeout(() => {
                wx.navigateBack();
            }, 1500);
        } catch (err) {
            console.error(err);
        } finally {
            this.setData({ loading: false });
        }
    }
});