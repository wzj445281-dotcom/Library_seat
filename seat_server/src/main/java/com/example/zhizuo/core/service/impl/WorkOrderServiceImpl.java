package com.example.zhizuo.core.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.WorkOrder; // 需创建Entity
import com.example.zhizuo.core.mapper.WorkOrderMapper; // 需创建Mapper
import com.example.zhizuo.core.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// 假设使用了 WebSocket
// import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {
    // 修改后 (使用服务名，docker-compose 会自动解析 IP)
    // 注意：如果您的 docker-compose.yml 里服务名是 ai-service，请用 ai-service
    // 根据之前的日志，服务名很可能是 ai-service
    private static final String AI_ANALYSIS_URL = "http://ai-service:5000/analyze/ticket";
    /**
     * 提交智能工单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTicket(Long userId, String category, String content, String imgUrl) {
        WorkOrder ticket = new WorkOrder();
        ticket.setTicketNo("WO" + IdUtil.getSnowflakeNextIdStr());
        ticket.setUserId(userId);
        ticket.setCategory(category);
        ticket.setContent(content);
        ticket.setSnapshotImg(imgUrl);
        ticket.setStatus("PENDING");
        ticket.setCreateTime(LocalDateTime.now());

        // 先落库，默认优先级为 0
        ticket.setPriority(0);
        this.save(ticket);

        // 异步调用 AI 进行分析，以免阻塞主线程
        analyzeTicketAsync(ticket);
    }

    /**
     * 异步 AI 分析
     * 调用 Python 服务分析文本情感和图片内容
     */
    @Async
    public void analyzeTicketAsync(WorkOrder ticket) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("text", ticket.getContent());
            params.put("imgUrl", ticket.getSnapshotImg());
            params.put("category", ticket.getCategory());

            // 调用 Python 接口
            String resultJson = HttpUtil.post(AI_ANALYSIS_URL, JSONUtil.toJsonStr(params));
            JSONObject result = JSONUtil.parseObj(resultJson);

            if (result.getInt("code") == 200) {
                JSONObject data = result.getJSONObject("data");
                int aiPriority = data.getInt("priority"); // 0, 1, 2
                String summary = data.getStr("summary");

                // 更新工单状态
                ticket.setPriority(aiPriority);
                ticket.setAiAnalysisResult(summary);
                this.updateById(ticket);

                // 如果是高优先级，立即通过 WebSocket 推送给管理员
                if (aiPriority >= 2) {
                    notifyAdmin(ticket);
                }
            }
        } catch (Exception e) {
            log.error("AI 工单分析失败 ticketNo: {}", ticket.getTicketNo(), e);
        }
    }

    private void notifyAdmin(WorkOrder ticket) {
        // WebSocket 推送逻辑示例
        // String dest = "/topic/admin/orders";
        // String message = "紧急工单提醒: " + ticket.getCategory() + " - " + ticket.getAiAnalysisResult();
        // messagingTemplate.convertAndSend(dest, message);
        log.info("【紧急播报】管理员请注意，收到高优先级工单: {}", ticket.getTicketNo());
    }
}