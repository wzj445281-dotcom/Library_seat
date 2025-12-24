package com.petsaas.controller.app;

import com.petsaas.service.MerchantService;
import com.petsaas.vo.MerchantVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app/shops")
public class AppShopController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping
    public Map<String, Object> getNearbyShops(
            @RequestParam("userLat") BigDecimal userLat,
            @RequestParam("userLng") BigDecimal userLng) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 校验参数，防止空指针
        if (userLat == null || userLng == null) {
            result.put("code", 400);
            result.put("message", "无法获取您的位置");
            return result;
        }

        List<MerchantVo> list = merchantService.selectNearby(userLat, userLng);
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", list);
        return result;
    }
}