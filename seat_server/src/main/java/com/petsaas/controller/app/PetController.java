package com.petsaas.controller.app;

import com.petsaas.dto.AiDiagnosisVo;
import com.petsaas.dto.ConsultReq;
import com.petsaas.service.PetDoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app/pet/doctor")
public class PetController {

    @Autowired
    private PetDoctorService petDoctorService;

    @PostMapping("/consult")
    public Map<String, Object> consult(@RequestBody ConsultReq req) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            AiDiagnosisVo diagnosisVo = petDoctorService.consult(req.getPetId(), req.getSymptoms());
            result.put("code", 200);
            result.put("message", "AI诊断成功");
            result.put("data", diagnosisVo);
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return result;
        }
    }
}