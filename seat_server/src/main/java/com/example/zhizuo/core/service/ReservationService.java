package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约服务接口
 * 继承 IService 获得基础 CRUD 能力
 */
public interface ReservationService extends IService<Reservation> {

    /**
     * 根据学号查询用户的预约记录 (小程序端"我的预约"使用)
     * @param studentId 学号
     * @return 预约列表
     */
    List<Reservation> getUserReservations(String studentId);

    /**
     * 核心业务：预约座位
     * @param userId 用户ID
     * @param seatId 座位ID
     * @param start 开始时间
     * @param end 结束时间
     */
    void reserve(Long userId, Long seatId, LocalDateTime start, LocalDateTime end);

    /**
     * 核心业务：签到
     * @param reservationId 预约ID
     */
    void checkIn(Long reservationId);

    /**
     * 核心业务：取消预约
     * @param reservationId 预约ID
     */
    void cancel(Long reservationId);

    /**
     * 核心业务：离座 (结束使用)
     * @param reservationId 预约ID
     */
    void leave(Long reservationId);

    /**
     * 定时任务：处理违约 (无需参数，由定时器自动调用)
     */
    void processNoShows();
}