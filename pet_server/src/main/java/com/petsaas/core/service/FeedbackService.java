package com.petsaas.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.core.entity.Feedback;

/**
 * <p>
 * 反馈服务接口
 * (补全缺失的接口文件)
 * </p>
 *
 * @author Petsaas
 * @since 2025-12-25
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 提交反馈
     * @param userId 用户ID
     * @param content 反馈内容
     * @param type 反馈类型
     * @param contact 联系方式
     * @return 是否成功
     */
    boolean submitFeedback(Long userId, String content, String type, String contact);
}