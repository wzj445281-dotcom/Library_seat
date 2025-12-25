const app = getApp()
import doctorApi from '../../api/doctor'

Page({
    data: {
        inputContent: '',
        scrollTop: 0,
        loading: false, // AI正在思考的状态
        msgList: [
            {
                id: 1,
                type: 'robot',
                content: '你好！我是 PetSaaS 的 AI 助理医生。你的宠物有什么不舒服，或者有关于养宠的问题，都可以问我哦！🐶🐱'
            }
        ]
    },

    onLoad() {
        // 页面加载时可以做一些初始化，比如检查用户是否登录
    },

    /**
     * 监听输入框内容
     */
    onInput(e) {
        this.setData({
            inputContent: e.detail.value
        })
    },

    /**
     * 发送消息
     */
    async sendMsg() {
        const content = this.data.inputContent.trim()
        if (!content) {
            wx.showToast({ title: '请输入问题', icon: 'none' })
            return
        }
        if (this.data.loading) {
            wx.showToast({ title: '医生正在思考中...', icon: 'none' })
            return
        }

        // 1. 用户消息立即上屏
        const userMsg = {
            id: Date.now(),
            type: 'user',
            content: content,
            avatar: wx.getStorageSync('userInfo')?.avatarUrl || '/assets/default_avatar.png' // 假设有默认头像
        }

        const newMsgList = [...this.data.msgList, userMsg]

        this.setData({
            msgList: newMsgList,
            inputContent: '',
            loading: true,
            scrollTop: newMsgList.length * 1000 // 简单粗暴滚动到底部
        })

        try {
            // 2. 调用后端 AI 接口
            const res = await doctorApi.askAiDoctor(content)

            // 3. 处理 AI 响应
            const aiResponseContent = res.data && res.data.answer ? res.data.answer : '抱歉，我现在有点累，请稍后再试。'

            const aiMsg = {
                id: Date.now() + 1,
                type: 'robot',
                content: aiResponseContent
            }

            this.setData({
                msgList: [...this.data.msgList, aiMsg],
                loading: false,
                scrollTop: (this.data.msgList.length + 1) * 1000
            })

        } catch (error) {
            console.error('AI问诊失败:', error)
            const errorMsg = {
                id: Date.now() + 1,
                type: 'robot',
                content: '网络连接异常，请检查服务器状态。'
            }
            this.setData({
                msgList: [...this.data.msgList, errorMsg],
                loading: false,
                scrollTop: (this.data.msgList.length + 1) * 1000
            })
        }
    }
})