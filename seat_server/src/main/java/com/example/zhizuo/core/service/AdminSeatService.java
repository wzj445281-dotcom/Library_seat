package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Seat;

import java.util.List;

/**
 * 管理端座位服务接口
 * 继承 IService 以获得 MyBatis Plus 的基础 CRUD 能力
 */
public interface AdminSeatService extends IService<Seat> {

    /**
     * 批量生成座位
     * @param row 排号
     * @param count 每排座位数
     */
    void batchCreateSeats(int row, int count);

    /**
     * 获取所有座位列表
     * @return 座位列表
     */
    List<Seat> getAllSeats();
}