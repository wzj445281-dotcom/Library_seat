package com.petsaas.service;

import com.petsaas.dto.AiDiagnosisVo;
import com.petsaas.mapper.PetMapper;
import com.petsaas.common.domain.Pet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.HashMap;
import java.util.Map;

@Service
public class PetDoctorService {

    private static final Logger log = LoggerFactory.getLogger(PetDoctorService.class);

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PetMapper petMapper;

    // Python 服务的地址 (如果部署在同一台服务器就是 localhost) 
    private static final String AI_SERVICE_URL = "http://localhost:5000/api/consult";

    public AiDiagnosisVo consult(Long petId, String symptoms) {
        // 1. 根据 petId 查询宠物详细信息 (品种、年龄等) 
        Pet pet = petMapper.selectById(petId);
        
        // 如果找不到宠物信息，使用默认值
        String petType = "宠物";
        Integer petAge = 1;
        
        if (pet != null) {
            petType = pet.getPetType();
            petAge = pet.getAge();
        }

        // 2. 封装请求给 Python 
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("petType", petType);
        requestMap.put("petAge", petAge);
        requestMap.put("symptoms", symptoms);

        try {
            // 3. 发起 HTTP POST 请求 
            // 这里的 String.class 表示我们先拿原生 JSON 字符串，也可以定义 DTO 自动接 
            ResponseEntity<String> response = restTemplate.postForEntity(
                AI_SERVICE_URL,
                requestMap,
                String.class
            );

            // 4. 解析 Python 返回的 JSON (这里用 Hutool 或 Jackson) 
            JSONObject json = JSONUtil.parseObj(response.getBody());
            if (json.getInt("code") == 200) {
                JSONObject data = json.getJSONObject("data");
                
                AiDiagnosisVo vo = new AiDiagnosisVo();
                vo.setAdvice(data.getStr("advice"));
                vo.setDiagnosis(data.getStr("diagnosis"));
                vo.setRecommendedService(data.getStr("recommendedService"));
                return vo;
            } else {
                throw new RuntimeException("AI 医生暂时掉线了");
            }

        } catch (Exception e) {
            log.error("调用 AI 服务异常", e);
            throw new RuntimeException("AI 服务响应超时，请稍后重试");
        }
    }
}