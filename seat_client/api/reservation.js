import request from '../utils/request.js';

// 提交预约
export const reserveSeat = (userId, seatId, start, end) => {
    const url = `/app/reservation/reserve?userId=${userId}&seatId=${seatId}&start=${start}&end=${end}`;
    return request(url, 'POST');
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