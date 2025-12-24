package com.example.zhizuo.api.app;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ProductService;
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
    private final ProductService productService;

    // Google Gemini API URL
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    // 请在 application.yml 中配置 your_google_api_key
    @Value("${ai.gemini.api-key:YOUR_API_KEY_HERE}")
    private String apiKey;

    public AiAssistantController(UserMapper userMapper, ProductService productService) {
        this.userMapper = userMapper;
        this.productService = productService;
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

        // 2. 检查是否需要商品推荐
        String productContext = "";
        if (userQuestion.contains("买") || userQuestion.contains("推荐") || userQuestion.contains("吃什么") || userQuestion.contains("用品")) {
            // 查询销量最高的3个商品
            List<Product> topProducts = productService.getTopSellingProducts(3);
            if (!topProducts.isEmpty()) {
                productContext = "本店热销商品推荐：\n";
                for (int i = 0; i < topProducts.size(); i++) {
                    Product p = topProducts.get(i);
                    String categoryName = p.getCategory() == 0 ? "主粮" : 
                                         p.getCategory() == 1 ? "零食" : 
                                         p.getCategory() == 2 ? "玩具" : "医疗";
                    productContext += String.format("%d. %s (%s) - ￥%.2f\n", 
                            i+1, p.getName(), categoryName, p.getPrice());
                }
                productContext += "\n";
            }
        }

        // 3. 构建 Prompt
        String systemPrompt = String.format(
                "你是一位专业的宠物医生助理，名字叫'宠爱小助手'。你的职责是：1.根据用户的描述判断宠物的健康状况。2.推荐本店的宠物商品（如狗粮、驱虫药）。3.引导用户预约线下的洗澡或美容服务。请用温柔、关怀的语气回答。\n\n" +
                "当前用户：%s，当前会员积分：%d。\n\n" +
                "%s" +
                "请根据以上信息，回答用户的问题。如果涉及宠物健康问题，请给出专业建议；如果涉及商品推荐，请结合热销商品进行推荐；如果涉及服务预约，请引导用户预约洗澡或美容服务。" +
                "回答要简练、亲切，不要暴露系统内部数据结构。",
                user.getName(), user.getPoints(), productContext
        );

        // 4. 调用 Gemini API
        try {
            String responseText = callGeminiApi(systemPrompt, userQuestion);
            return ApiResponse.success(responseText);
        } catch (Exception e) {
            log.error("AI 服务调用失败", e);
            return ApiResponse.success("抱歉，AI 大脑暂时短路了，请稍后再试。您的当前会员积分是 " + user.getPoints());
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