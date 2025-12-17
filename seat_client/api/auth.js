import request from '../utils/request.js';

// 提交预约
// 后端接口: POST /api/app/reservation/reserve
// 入参: ReservationRequestDTO { userId, seatId, startTime, endTime }
export const reserveSeat = (userId, seatId, start, end) => {
    // 构造后端需要的 DTO 格式 (JSON Body)
    const data = {
        userId: userId, // 配合后端逻辑，虽然Token里有，但DTO里加上更灵活
        seatId: seatId,
        startTime: start,
        endTime: end
    };
    // request 默认 content-type 就是 application/json
    return request('/app/reservation/reserve', 'POST', data);
};

// 获取我的预约列表
export const getMyReservations = () => {
    return request('/app/reservation/list', 'GET');
};

// 签到
// 后端接口: POST /api/app/reservation/checkin/{id}
// 参数仍在路径上，保持不变
export const checkIn = (id) => {
    return request(`/app/reservation/checkin/${id}`, 'POST');
};

// 取消预约
// 后端接口: POST /api/app/reservation/cancel/{id}
export const cancelReservation = (id) => {
    return request(`/app/reservation/cancel/${id}`, 'POST');
};