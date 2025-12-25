package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户收藏表
 * </p>
 *
 * @author 10YearExpert
 * @since 2025-12-26
 */
@Data
@Accessors(chain = true) // 支持链式调用 set
@TableName("user_favorite")
public class UserFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID (对应 Auth 模块的 openid 或 userId)
     */
    private String userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
}