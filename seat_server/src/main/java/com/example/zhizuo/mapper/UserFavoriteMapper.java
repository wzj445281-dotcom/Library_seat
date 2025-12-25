package com.example.zhizuo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhizuo.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户收藏表 Mapper 接口
 * </p>
 *
 * @author 10YearExpert
 * @since 2025-12-26
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

}