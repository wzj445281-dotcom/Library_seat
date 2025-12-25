package com.petsaas.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.Feedback;
import com.petsaas.core.mapper.FeedbackMapper;
import com.petsaas.core.service.FeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
    // Mybatis-Plus 已自动实现基础 CRUD，如需复杂业务逻辑可在此扩展
    // 例如：当反馈提交后，自动发送邮件通知管理员
}