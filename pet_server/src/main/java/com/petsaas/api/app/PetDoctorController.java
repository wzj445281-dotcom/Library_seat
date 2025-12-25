package com.petsaas.api.app;

import com.petsaas.common.ApiResponse;
import com.petsaas.core.entity.PetDoctor;
import com.petsaas.core.service.PetDoctorService;
import com.petsaas.service.impl.DoctorScheduleServiceImpl; // 根据你上传的文件包名引用
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app/doctor")
@Tag(name = "App-宠物医生模块")
public class PetDoctorController {

    @Autowired
    private PetDoctorService petDoctorService;

    @Autowired
    private DoctorScheduleServiceImpl doctorScheduleService;

    @Operation(summary = "获取医生列表")
    @GetMapping("/list")
    public ApiResponse<List<PetDoctor>> getDoctorList() {
        return ApiResponse.success(petDoctorService.getActiveDoctors());
    }

    @Operation(summary = "获取医生排班/可用时间")
    @GetMapping("/schedule")
    public ApiResponse<List<String>> getSchedule(@RequestParam Long doctorId,
                                                 @RequestParam String date) {
        // date 格式: yyyy-MM-dd
        List<String> slots = doctorScheduleService.getAvailableSlots(doctorId, date);
        return ApiResponse.success(slots);
    }

    @Operation(summary = "AI 问诊")
    @PostMapping("/ask")
    public ApiResponse<Map<String, String>> askDoctor(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String answer = petDoctorService.consult(question);
        Map<String, Object> result = new HashMap<>();
        result.put("answer", answer);
        return ApiResponse.success(result);
    }
}