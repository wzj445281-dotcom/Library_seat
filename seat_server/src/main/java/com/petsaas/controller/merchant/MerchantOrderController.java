package com.petsaas.controller.merchant;

import com.petsaas.dto.VerifyReq;
import com.petsaas.service.MerchantOrderService;
import com.petsaas.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    @Autowired
    private MerchantOrderService merchantOrderService;

    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody VerifyReq req) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从请求头或SecurityContext获取当前商户ID
            Long currentMerchantId = SecurityUtils.getMerchantId();
            
            merchantOrderService.verifyOrder(currentMerchantId, req.getCode());
            
            result.put("code", 200);
            result.put("message", "核销成功，服务开始！");
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return result;
        }
    }
}