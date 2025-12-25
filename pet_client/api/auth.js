import request from '../utils/request.js';

/**
 * 提交预约
 * 修复点：
 * 1. 路径修正为 /app/reservation/book
 * 2. 参数修正为后端 @RequestBody Map 需要的字段 (slotId, petName, appointmentTime)
 * 3. userId 不需要传，后端通过 Token 获取
 */
export const reserveSeat = (slotId, petName, appointmentTime) => {
    const data = {
        slotId: slotId,                 // 对应后端 payload.get("slotId")
        petName: petName,               // 对应后端 payload.get("petName")
        appointmentTime: appointmentTime // 对应后端 payload.get("appointmentTime")，格式需为 "yyyy-MM-dd HH:mm:ss"
    };
    return request('/app/reservation/book', 'POST', data);
};

/**
 * 获取我的预约列表
 * 修复点：
 * 1. 路径修正为 /app/reservation/my (对应后端 @GetMapping("/my"))
 */
export const getMyReservations = () => {
    return request('/app/reservation/my', 'GET');
};

/**
 * 取消预约
 * 修复点：
 * 1. 路径修正为 /app/reservation/cancel (对应后端 @PostMapping("/cancel"))
 * 2. 参数改为 JSON Body 传递，而不是路径参数
 */
export const cancelReservation = (id) => {
    const data = {
        id: id // 对应后端 payload.get("id")
    };
    return request('/app/reservation/cancel', 'POST', data);
};

/**
 * 签到
 * 注意：你提供的 AppReservationController.java 中目前没有 checkin 接口。
 * 如果后端尚未实现，调用此接口会报 404。建议在后端补充对应方法。
 */
export const checkIn = (id) => {
    // 假设后端后续会实现为路径参数形式，暂时保持原样
    return request(`/app/reservation/checkin/${id}`, 'POST');
};