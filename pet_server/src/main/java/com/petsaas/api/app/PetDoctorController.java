package com.petsaas.api.app;

import com.petsaas.common.ApiResponse;
import com.petsaas.core.service.PetDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app/doctor")
public class PetDoctorController {

    @Autowired
    private PetDoctorService petDoctorService;

    @PostMapping("/ask")
    public ApiResponse askDoctor(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");

        // 调用 Service 获取真实 AI 回复
        String answer = petDoctorService.consult(question);

        Map<String, Object> result = new HashMap<>();
        result.put("answer", answer);
        return ApiResponse.success(result);
    }
}