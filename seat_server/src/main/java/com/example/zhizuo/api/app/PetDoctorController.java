package com.example.zhizuo.api.app; 
 
import com.example.zhizuo.common.ApiResponse; 
import org.springframework.web.bind.annotation.*; 
 
import java.util.HashMap; 
import java.util.Map; 
 
/** 
 * 宠物AI医生接口 
 */ 
@RestController 
@RequestMapping("/api/app/doctor") 
public class PetDoctorController { 
 
    @PostMapping("/ask") 
    public ApiResponse askDoctor(@RequestBody Map<String, String> payload) { 
        String question = payload.get("question"); 
         
        // 模拟 AI 逻辑 (实际可调用 OpenAI 或 阿里云百炼 API) 
        String answer; 
        if (question == null || question.isEmpty()) { 
            answer = "您好，我是您的专属宠物健康顾问。请问有什么可以帮您？"; 
        } else if (question.contains("吐") || question.contains("呕")) { 
            answer = "如果您的宠物出现呕吐症状，建议先禁食禁水4-6小时观察。如果呕吐物带血或伴有精神萎靡，请立即前往我们的【医疗工位】预约就诊！"; 
        } else if (question.contains("洗澡")) { 
            answer = "猫咪通常不需要频繁洗澡，建议每3-6个月一次。狗狗建议每1-2周一次。您可以使用我们的【自助洗护区】。"; 
        } else if (question.contains("掉毛")) { 
            answer = "换毛季掉毛是正常的。建议增加梳毛频率，并补充鱼油或卵磷脂。您可以查看商城中的【宠物鱼油】商品。"; 
        } else { 
            answer = "这是一个很好的问题。建议您带宠物来线下进行全面体检，或者详细描述症状，我会尽力为您解答。"; 
        } 
         
        Map<String, Object> result = new HashMap<>(); 
        result.put("answer", answer); 
        return ApiResponse.success(result); 
    } 
}