package com.petsaas.controller.merchant;

import com.petsaas.service.MerchantDashboardService;
import com.petsaas.util.SecurityUtils;
import com.petsaas.vo.DashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/dashboard")
public class MerchantDashboardController {

    @Autowired
    private MerchantDashboardService merchantDashboardService;

    @GetMapping
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从请求头或SecurityContext获取当前商户ID
            Long currentMerchantId = SecurityUtils.getMerchantId();
            
            DashboardVo dashboardData = merchantDashboardService.getDashboardData(currentMerchantId);
            
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", dashboardData);
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
            return result;
        }
    }
}