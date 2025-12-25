package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.UserFavorite;
import com.example.zhizuo.mapper.UserFavoriteMapper; // 假设你有Mapper接口，如果没有请新建
import com.example.zhizuo.common.util.SecurityUtils; // 假设你有获取当前用户的工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 小程序端 - 收藏功能接口
 */
@RestController
@RequestMapping("/api/app/favorite")
public class FavoriteController {

    @Autowired
    private UserFavoriteMapper favoriteMapper;

    /**
     * 切换收藏状态 (收藏/取消收藏)
     * 前端只需传 productId，后端自动判断是新增还是删除
     */
    @PostMapping("/toggle")
    public ApiResponse<Boolean> toggleFavorite(@RequestBody UserFavorite payload) {
        // 1. 获取当前登录用户
        String currentUserId = SecurityUtils.getCurrentUserId();
        Long productId = payload.getProductId();

        if (productId == null) {
            return ApiResponse.error("商品ID不能为空");
        }

        // 2. 查询是否已收藏
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, currentUserId)
                .eq(UserFavorite::getProductId, productId);

        UserFavorite existing = favoriteMapper.selectOne(query);

        boolean isFavorited;
        if (existing != null) {
            // 3. 已存在 -> 删除 (取消收藏)
            favoriteMapper.deleteById(existing.getId());
            isFavorited = false;
        } else {
            // 4. 不存在 -> 新增 (添加收藏)
            UserFavorite newFav = new UserFavorite();
            newFav.setUserId(currentUserId);
            newFav.setProductId(productId);
            newFav.setCreateTime(java.time.LocalDateTime.now());
            favoriteMapper.insert(newFav);
            isFavorited = true;
        }

        // TODO: 如果有 Redis 缓存商品详情，这里需要清除或更新缓存

        return ApiResponse.success(isFavorited);
    }

    /**
     * 获取我的收藏列表 (返回商品ID列表，前端根据ID匹配高亮)
     */
    @GetMapping("/ids")
    public ApiResponse<List<Long>> getMyFavoriteIds() {
        String currentUserId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.select(UserFavorite::getProductId) // 只查ID，性能更高
                .eq(UserFavorite::getUserId, currentUserId);

        List<Object> productIds = favoriteMapper.selectObjs(query);

        // 转换类型
        List<Long> result = productIds.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .collect(Collectors.toList());

        return ApiResponse.success(result);
    }
}