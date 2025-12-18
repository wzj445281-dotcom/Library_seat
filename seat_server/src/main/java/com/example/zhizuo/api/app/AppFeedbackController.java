package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Feedback;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.FeedbackMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/app/feedback")
@Tag(name = "App-反馈模块")
public class AppFeedbackController {

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    public AppFeedbackController(FeedbackMapper feedbackMapper, UserMapper userMapper) {
        this.feedbackMapper = feedbackMapper;
        this.userMapper = userMapper;
    }

    @Operation(summary = "提交反馈/报修")
    @PostMapping("/submit")
    public ApiResponse<String> submit(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        String contact = params.get("contact");

        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "内容不能为空");
        }

        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = (String) auth.getPrincipal();
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        User user = userMapper.selectOne(query);

        Feedback fb = new Feedback();
        fb.setUserId(user.getId());
        fb.setContent(content);
        fb.setContact(contact);
        fb.setStatus(0); // 默认未处理
        fb.setCreateTime(LocalDateTime.now());

        feedbackMapper.insert(fb);
        return ApiResponse.success("提交成功，我们会尽快处理");
    }
}