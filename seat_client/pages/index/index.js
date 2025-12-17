import { getSeatLayout } from '../../api/seat.js';
import { reserveSeat } from '../../api/reservation.js';
import { formatDate } from '../../utils/util.js'; // 假设你有这个工具，如果没有就在下面手写

Page({
    data: {
        seats: [],
        selectedSeat: null
    },

    onShow() {
        // 每次显示页面时刷新座位状态
        this.loadSeats();
    },

    async loadSeats() {
        try {
            wx.showLoading({ title: '加载中...' });
            const seatList = await getSeatLayout();
            this.setData({ seats: seatList, selectedSeat: null });
            wx.hideLoading();
        } catch (err) {
            wx.hideLoading();
            // 错误处理已经在 request.js 做过了
        }
    },

    handleSeatClick(e) {
        const index = e.currentTarget.dataset.index;
        const seat = this.data.seats[index];

        // 如果座位维修(0)或者已被占用(逻辑上如果后端返回了status标记)，则不可选
        // 假设后端返回 status: 1=可用, 0=维修
        if (seat.status === 0) {
            return wx.showToast({ title: '该座位维护中', icon: 'none' });
        }

        // 切换选中状态：如果点的是同一个，就取消选中
        if (this.data.selectedSeat && this.data.selectedSeat.id === seat.id) {
            this.setData({ selectedSeat: null });
        } else {
            this.setData({ selectedSeat: seat });
        }
    },

    async handleSubmit() {
        if (!this.data.selectedSeat) return;

        // 构造时间：当前时间 -> 2小时后
        const now = new Date();
        const start = this.formatTime(now);
        const end = this.formatTime(new Date(now.getTime() + 2 * 60 * 60 * 1000)); // +2小时

        const userId = wx.getStorageSync('userId');
        if (!userId) {
            return wx.showToast({ title: '请先登录', icon: 'none' });
        }

        try {
            wx.showLoading({ title: '预约中...' });

            await reserveSeat(
                userId,
                this.data.selectedSeat.id,
                start,
                end
            );

            wx.hideLoading();
            wx.showToast({ title: '预约成功！', icon: 'success' });

            // 预约成功后，刷新座位图，并清空选择
            this.setData({ selectedSeat: null });
            this.loadSeats();

        } catch (err) {
            wx.hideLoading();
            console.error(err);
        }
    },
// 增加 handleSmartRecommend 方法
    handleSmartRecommend() {
        const seats = this.data.seats;
        // 过滤出可用的座位
        const availableSeats = seats.filter(s => s.status === 1);

        if (availableSeats.length === 0) {
            return wx.showToast({ title: '暂无可用座位', icon: 'none' });
        }

        wx.showLoading({ title: 'AI 计算中...' });

        // 模拟 AI 决策延迟
        setTimeout(() => {
            // 策略：找热度分最低的（假设这里的逻辑是找最安静的角落）
            // 也可以改为找 heatScore 最高的（找热门区域）
            // 这里我们需要后端返回 heatScore，之前的 AppSeatController 改造已经加上了

            // 排序：按热度分从小到大
            availableSeats.sort((a, b) => (a.heatScore || 0) - (b.heatScore || 0));

            const recommendSeat = availableSeats[0]; // 推荐最安静的

            this.setData({
                selectedSeat: recommendSeat
            });

            wx.hideLoading();
            wx.showModal({
                title: 'AI 推荐成功',
                content: `为您推荐了最安静的座位：${recommendSeat.label} (热度分: ${recommendSeat.heatScore || 0})`,
                showCancel: false
            });
        }, 800);
    },
    // 简单的格式化时间工具 yyyy-MM-dd HH:mm:ss
    formatTime(date) {
        const y = date.getFullYear();
        const m = (date.getMonth() + 1).toString().padStart(2, '0');
        const d = date.getDate().toString().padStart(2, '0');
        const h = date.getHours().toString().padStart(2, '0');
        const min = date.getMinutes().toString().padStart(2, '0');
        const s = date.getSeconds().toString().padStart(2, '0');
        return `${y}-${m}-${d} ${h}:${min}:${s}`;
    }
});