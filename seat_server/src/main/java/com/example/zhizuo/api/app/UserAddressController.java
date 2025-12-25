package com.example.zhizuo.api.app;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.common.util.SecurityUtils;
import com.example.zhizuo.core.entity.UserAddress;
import com.example.zhizuo.core.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/app/address")
@Tag(name = "App-地址管理")
public class UserAddressController {

    @Autowired
    private UserAddressService addressService;

    @Operation(summary = "获取地址列表")
    @GetMapping("/list")
    public ApiResponse<List<UserAddress>> getAddressList() {
        Long userId = SecurityUtils.getUserId();
        List<UserAddress> addresses = addressService.getUserAddresses(userId);
        return ApiResponse.success(addresses);
    }

    @Operation(summary = "获取默认地址")
    @GetMapping("/default")
    public ApiResponse<UserAddress> getDefaultAddress() {
        Long userId = SecurityUtils.getUserId();
        UserAddress address = addressService.getDefaultAddress(userId);
        return ApiResponse.success(address);
    }

    @Operation(summary = "添加地址")
    @PostMapping("/add")
    public ApiResponse<Long> addAddress(@RequestBody @Validated UserAddress address) {
        Long userId = SecurityUtils.getUserId();
        Long addressId = addressService.addAddress(userId, address);
        return ApiResponse.success(addressId);
    }

    @Operation(summary = "更新地址")
    @PutMapping("/update/{id}")
    public ApiResponse<String> updateAddress(@PathVariable Long id, @RequestBody @Validated UserAddress address) {
        Long userId = SecurityUtils.getUserId();
        addressService.updateAddress(userId, id, address);
        return ApiResponse.success("更新成功");
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        addressService.deleteAddress(userId, id);
        return ApiResponse.success("删除成功");
    }

    @Operation(summary = "设置默认地址")
    @PutMapping("/set-default/{id}")
    public ApiResponse<String> setDefaultAddress(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        addressService.setDefaultAddress(userId, id);
        return ApiResponse.success("设置成功");
    }
}

