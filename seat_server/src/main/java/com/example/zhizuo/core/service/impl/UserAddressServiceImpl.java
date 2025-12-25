package com.example.zhizuo.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.UserAddress;
import com.example.zhizuo.core.mapper.UserAddressMapper;
import com.example.zhizuo.core.service.UserAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddress> getUserAddresses(Long userId) {
        LambdaQueryWrapper<UserAddress> query = new LambdaQueryWrapper<>();
        query.eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);
        return list(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(Long userId, UserAddress address) {
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault()) {
            cancelOtherDefaultAddresses(userId);
        }

        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        save(address);

        log.info("添加地址成功：用户ID={}, 地址ID={}", userId, address.getId());
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Long addressId, UserAddress address) {
        // 验证地址归属
        UserAddress existing = getById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限");
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault()) {
            cancelOtherDefaultAddresses(userId, addressId);
        }

        address.setId(addressId);
        address.setUserId(userId);
        address.setUpdateTime(LocalDateTime.now());
        updateById(address);

        log.info("更新地址成功：用户ID={}, 地址ID={}", userId, addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long addressId) {
        // 验证地址归属
        UserAddress existing = getById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限");
        }

        removeById(addressId);
        log.info("删除地址成功：用户ID={}, 地址ID={}", userId, addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long addressId) {
        // 验证地址归属
        UserAddress existing = getById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权限");
        }

        // 取消其他默认地址
        cancelOtherDefaultAddresses(userId, addressId);

        // 设置当前地址为默认
        existing.setIsDefault(true);
        existing.setUpdateTime(LocalDateTime.now());
        updateById(existing);

        log.info("设置默认地址成功：用户ID={}, 地址ID={}", userId, addressId);
    }

    @Override
    public UserAddress getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> query = new LambdaQueryWrapper<>();
        query.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, true)
                .last("LIMIT 1");
        return getOne(query);
    }

    /**
     * 取消其他默认地址
     */
    private void cancelOtherDefaultAddresses(Long userId) {
        cancelOtherDefaultAddresses(userId, null);
    }

    /**
     * 取消其他默认地址（排除指定地址ID）
     */
    private void cancelOtherDefaultAddresses(Long userId, Long excludeId) {
        LambdaUpdateWrapper<UserAddress> update = new LambdaUpdateWrapper<>();
        update.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, true)
                .set(UserAddress::getIsDefault, false)
                .set(UserAddress::getUpdateTime, LocalDateTime.now());
        
        if (excludeId != null) {
            update.ne(UserAddress::getId, excludeId);
        }
        
        update(update);
    }
}

