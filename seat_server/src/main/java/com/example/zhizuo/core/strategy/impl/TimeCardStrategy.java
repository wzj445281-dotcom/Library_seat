package com.example.zhizuo.core.strategy.impl;

import com.example.zhizuo.core.strategy.BenefitStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 时间卡/权益卡策略实现
 */
@Slf4j
@Component("timeCardStrategy")
public class TimeCardStrategy implements BenefitStrategy {

    @Override
    public void process(Long userId, Object... extraParams) {
        log.info("执行时间卡权益策略, userId: {}", userId);

        // 解析参数 (示例逻辑)
        // 假设 extraParams[0] 是增加的时长(小时)
        if (extraParams != null && extraParams.length > 0) {
            try {
                Object param = extraParams[0];
                log.info("增加时长参数: {}", param);

                // TODO: 在这里添加具体的业务逻辑
                // 例如：调用 userService.addTime(userId, (Integer) param);

            } catch (Exception e) {
                log.error("处理时间卡权益失败", e);
            }
        }
    }
}