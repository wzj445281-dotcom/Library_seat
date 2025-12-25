package com.petsaas.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.core.entity.Feedback;
import com.petsaas.core.mapper.FeedbackMapper;
import com.petsaas.core.service.FeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
    
    @Autowired
    private FeedbackMapper feedbackMapper;
    
    @Override
    public boolean submitFeedback(Long userId, String content, String type, String contact) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(content);
        feedback.setType(type);
        feedback.setContact(contact);
        feedback.setCreateTime(LocalDateTime.now());
        
        return feedbackMapper.insert(feedback) > 0;
    }
}