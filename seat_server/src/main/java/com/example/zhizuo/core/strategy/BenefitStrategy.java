package com.example.zhizuo.core.strategy;

/**
 * 权益/道具使用策略接口
 */
public interface BenefitStrategy {

    /**
     * 处理权益发放或使用
     * @param userId 用户ID
     * @param extraParams 额外参数（如时长、数量等），可视具体实现调整
     */
    void process(Long userId, Object... extraParams);
}