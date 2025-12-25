package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.UserAddress;

import java.util.List;

/**
 * 用户地址服务接口
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> getUserAddresses(Long userId);

    /**
     * 添加地址
     *
     * @param userId 用户ID
     * @param address 地址信息
     * @return 地址ID
     */
    Long addAddress(Long userId, UserAddress address);

    /**
     * 更新地址
     *
     * @param userId 用户ID
     * @param addressId 地址ID
     * @param address 地址信息
     */
    void updateAddress(Long userId, Long addressId, UserAddress address);

    /**
     * 删除地址
     *
     * @param userId 用户ID
     * @param addressId 地址ID
     */
    void deleteAddress(Long userId, Long addressId);

    /**
     * 设置默认地址
     *
     * @param userId 用户ID
     * @param addressId 地址ID
     */
    void setDefaultAddress(Long userId, Long addressId);

    /**
     * 获取默认地址
     *
     * @param userId 用户ID
     * @return 默认地址，如果没有则返回null
     */
    UserAddress getDefaultAddress(Long userId);
}

