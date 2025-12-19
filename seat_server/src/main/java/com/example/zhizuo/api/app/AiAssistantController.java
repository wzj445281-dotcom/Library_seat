package com.example.zhizuo.api.app;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.CreditLog;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.CreditLogMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/app/ai")
@Tag(name = "App-AI客服助手")
public class AiAssistantController {

    private final UserMapper userMapper;
    private final CreditLogMapper creditLogMapper;

    // Google Gemini API URL
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    // 请在 application.yml 中配置 your_google_api_key
    @Value("${ai.gemini.api-key:YOUR_API_KEY_HERE}")
    private String apiKey;

    public AiAssistantController(UserMapper userMapper, CreditLogMapper creditLogMapper) {
        this.userMapper = userMapper;
        this.creditLogMapper = creditLogMapper;
    }

    @Operation(summary = "咨询AI客服")
    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody Map<String, String> params) {
        String userQuestion = params.get("message");
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            return ApiResponse.error(400, "问题不能为空");
        }

        // 1. 获取用户上下文
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = (String) auth.getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("student_id", studentId));

        // 2. 获取最近5条信用日志
        List<CreditLog> logs = creditLogMapper.selectList(
                new QueryWrapper<CreditLog>()
                        .eq("user_id", user.getId())
                        .orderByDesc("create_time")
                        .last("LIMIT 5")
        );

        // 格式化日志为字符串
        String logContext = logs.stream()
                .map(l -> String.format("[%s] %s %d分 (原因: %s)",
                        l.getCreateTime().toString(),
                        "ADD".equals(l.getType()) ? "加" : "扣",
                        l.getScore(),
                        l.getReason()))
                .collect(Collectors.joining("\n"));

        // 3. 构建 Prompt
        String systemPrompt = String.format(
                "你是一个高校图书馆座位预约系统的智能客服‘智座小助手’。当前对话学生：%s，当前信用分：%d。\n" +
                        "该学生的最近信用变动记录如下：\n%s\n\n" +
                        "请根据以上信息，回答学生的问题。如果涉及扣分，请温和地解释原因；如果信用分较低，请给出恢复建议（如连续签到、正常离座）。" +
                        "回答要简练、亲切，不要暴露系统内部数据结构。",
                user.getName(), user.getCreditScore(), logContext
        );

        // 4. 调用 Gemini API
        try {
            String responseText = callGeminiApi(systemPrompt, userQuestion);
            return ApiResponse.success(responseText);
        } catch (Exception e) {
            log.error("AI 服务调用失败", e);
            return ApiResponse.success("抱歉，AI 大脑暂时短路了，请稍后再试。您的当前信用分是 " + user.getCreditScore());
        }
    }

    private String callGeminiApi(String systemPrompt, String userMessage) {
        // 构建 Gemini 请求体
        JSONObject content = new JSONObject();

        JSONObject partSystem = new JSONObject().set("text", systemPrompt + "\n\n用户提问：" + userMessage);

        JSONArray parts = new JSONArray().put(partSystem);
        JSONObject contentsObj = new JSONObject().set("parts", parts);

        content.set("contents", new JSONArray().put(contentsObj));

        String url = GEMINI_API_URL + "?key=" + apiKey;

        HttpResponse response = HttpRequest.post(url)
                .body(content.toString())
                .timeout(10000)
                .execute();

        if (response.getStatus() == 200) {
            JSONObject jsonRes = JSONUtil.parseObj(response.body());
            // 解析 Gemini 响应结构
            // candidates[0].content.parts[0].text
            try {
                return jsonRes.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getStr("text");
            } catch (Exception e) {
                return "解析 AI 响应失败";
            }
        } else {
            log.error("Gemini API Error: {} {}", response.getStatus(), response.body());
            throw new RuntimeException("API调用失败");
        }
    }
}