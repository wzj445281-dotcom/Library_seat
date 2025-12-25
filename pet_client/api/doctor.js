import request from '../utils/request'
import {getMyReservations} from "./auth";

/**
 * 医生服务 API (完整版)
 */
export default {
    login(data) {
        return request({ url: '/api/app/auth/login', method: 'POST', data })
    },
    /**
     * 获取活跃医生列表
     */
    getDoctorList() {
        return request({
            url: '/api/app/doctor/list',
            method: 'GET'
        })
    },

    /**
     * 获取医生详情 (如果不通过列表传参，可单独调用)
     */
    getDoctorDetail(id) {
        return request({
            url: `/api/app/doctor/${id}`,
            method: 'GET'
        })
    },

    /**
     * 获取医生排班信息
     * @param {number} doctorId 医生ID
     * @param {string} date 日期 (YYYY-MM-DD)
     */
    getSchedule(doctorId, date) {
        return request({
            url: '/api/app/doctor/schedule',
            method: 'GET',
            data: {
                doctorId,
                date
            }
        })
    },

    /**
     * AI 智能问诊
     */
    askAiDoctor(question) {
        return request({
            url: '/api/app/doctor/ask',
            method: 'POST',
            data: {
                question
            }
        })
    },
    /**
     * 提交预约
     * @param {object} data { doctorId, startTime, note }
     */
    createReservation(data) {
        return request({
            url: '/api/app/reservation/create',
            method: 'POST',
            data: data
        })
    },
}/**
 * 获取我的预约记录
 */
getMyReservations() {
    return request({
        url: '/api/app/reservation/my',
        method: 'GET'
    })
},
}