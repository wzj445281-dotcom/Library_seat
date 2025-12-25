package com.example.zhizuo.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户领券记录实体
 */
@Data
@Accessors(chain = true)
@TableName("user_coupon")
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 关联的优惠券模板ID
     */
    private Long couponId;

    /**
     * 关联的订单ID (使用后回填)
     */
    private Long orderId;

    /**
     * 状态 0:未使用 1:已使用 2:已过期
     */
    private Integer status;

    /**
     * 领取时间
     */
    private LocalDateTime createTime;

    /**
     * 使用时间
     */
    private LocalDateTime useTime;
}