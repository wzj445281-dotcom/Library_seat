package com.example.zhizuo.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Feedback;
import com.example.zhizuo.core.mapper.FeedbackMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedback")
@Tag(name = "Admin-反馈管理")
public class AdminFeedbackController {

    private final FeedbackMapper feedbackMapper;

    public AdminFeedbackController(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @Operation(summary = "分页查询反馈列表")
    @GetMapping("/list")
    public ApiResponse<Page<Feedback>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        Page<Feedback> pageParam = new Page<>(page, size);
        QueryWrapper<Feedback> query = new QueryWrapper<>();
        if (status != null) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time"); // 最新的在前面
        return ApiResponse.success(feedbackMapper.selectPage(pageParam, query));
    }

    @Operation(summary = "处理反馈")
    @PostMapping("/resolve/{id}")
    public ApiResponse<String> resolve(@PathVariable Long id) {
        Feedback fb = feedbackMapper.selectById(id);
        if (fb != null) {
            fb.setStatus(1); // 标记为已处理
            feedbackMapper.updateById(fb);
        }
        return ApiResponse.success("操作成功");
    }
}