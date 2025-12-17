package com.example.zhizuo.api.admin;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.service.AiPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/demo")
@Tag(name = "Admin-演示控制台")
public class DemoController {

    private final AiPredictionService aiPredictionService;

    public DemoController(AiPredictionService aiPredictionService) {
        this.aiPredictionService = aiPredictionService;
    }

    @Operation(summary = "手动触发AI热度同步")
    @PostMapping("/trigger-ai-sync")
    public ApiResponse<String> triggerAiSync() {
        // 立即执行一次热度预测
        aiPredictionService.syncHeatScores();
        return ApiResponse.success("AI 预测任务已手动触发，请刷新大屏查看效果");
    }
}