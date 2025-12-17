import request from '../utils/request.js';

// 提交预约 (改为 POST JSON Body，适配后端 ReservationRequestDTO)
export const reserveSeat = (userId, seatId, start, end) => {
    // 构造后端需要的 DTO 格式
    const data = {
        userId: userId, // 其实后端可以从Token拿，但为了兼容性先传
        seatId: seatId,
        startTime: start,
        endTime: end
    };
    return request('/app/reservation/reserve', 'POST', data);
};

// 获取我的预约列表
export const getMyReservations = () => {
    return request('/app/reservation/list', 'GET');
};

// 签到
export const checkIn = (id) => {
    return request(`/app/reservation/checkin/${id}`, 'POST');
};

// 取消预约
export const cancelReservation = (id) => {
    return request(`/app/reservation/cancel/${id}`, 'POST');
};