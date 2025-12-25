package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Order;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取管理员订单列表
     *
     * @param status 订单状态
     * @return 订单列表统计信息
     */
    Map<String, Object> getAdminOrderList(String status);

    /**
     * 更新订单状态
     *
     * @param id 订单ID
     * @param status 新状态
     */
    void updateOrderStatus(Long id, String status);

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param items 订单项列表
     * @param deliveryType 配送方式
     * @param userCouponId 使用的优惠券ID（可选）
     * @param addressInfo 地址信息（外卖时必需）
     * @param remark 备注
     * @return 订单号
     */
    String createOrder(Long userId, List<Map<String, Object>> items, Integer deliveryType, Long userCouponId, String addressInfo, String remark);

    /**
     * 关闭订单并恢复库存
     *
     * @param orderId 订单ID
     * @param reason 关闭原因
     */
    void closeOrderAndRestoreStock(Long orderId, String reason);

    /**
     * 获取用户订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getUserOrderList(Long userId, String status);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单对象
     */
    Order getByOrderNo(String orderNo);
}