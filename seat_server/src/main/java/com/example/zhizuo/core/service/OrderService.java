package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Order;

/**
 * <p>
 * 订单表 服务类
 * </p>
 */
public interface OrderService extends IService<Order> {

    /**
     * 分页查询订单（包含商品明细）
     * 解决 MyBatis Plus 默认查询不带子表的问题
     *
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 带 items 的分页结果
     */
    IPage<Order> pageWithItems(Page<Order> page, Wrapper<Order> queryWrapper);

    /**
     * 查询订单详情（包含商品明细）
     *
     * @param id 订单ID
     * @return 带 items 的订单对象
     */
    Order getDetailWithItems(Long id);
}