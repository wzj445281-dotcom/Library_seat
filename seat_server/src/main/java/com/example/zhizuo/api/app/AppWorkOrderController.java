package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.entity.WorkOrder;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 智能工单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/app/feedback")
@Tag(name = "App-智能反馈")
public class AppWorkOrderController {

    @Resource
    private WorkOrderService workOrderService;
    @Resource
    private UserMapper userMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = (String) auth.getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("student_id", studentId));
        return user.getId();
    }

    @Operation(summary = "提交智能工单")
    @PostMapping("/submit")
    public ApiResponse<String> submit(@RequestBody Map<String, String> params) {
        String category = params.get("category");
        String content = params.get("content");
        String imgUrl = params.get("imgUrl");

        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "反馈内容不能为空");
        }

        try {
            workOrderService.submitTicket(getCurrentUserId(), category, content, imgUrl);
            return ApiResponse.success("提交成功，AI正在分析处理中...");
        } catch (Exception e) {
            log.error("工单提交失败", e);
            return ApiResponse.error(500, "系统繁忙，请重试");
        }
    }

    @Operation(summary = "我的反馈记录")
    @GetMapping("/history")
    public ApiResponse<List<WorkOrder>> history() {
        QueryWrapper<WorkOrder> query = new QueryWrapper<>();
        query.eq("user_id", getCurrentUserId());
        query.orderByDesc("create_time");
        return ApiResponse.success(workOrderService.list(query));
    }
}