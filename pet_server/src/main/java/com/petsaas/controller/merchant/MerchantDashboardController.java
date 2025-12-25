package com.petsaas.controller.merchant;

import com.petsaas.common.ApiResponse;
import com.petsaas.service.MerchantDashboardService;
import com.petsaas.vo.DashboardVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/dashboard")
@Tag(name = "Merchant-数据驾驶舱")
public class     {

@Autowired
private MerchantDashboardService dashboardService;

@Operation(summary = "获取今日经营数据")
@GetMapping("/stats")
public ApiResponse<DashboardVo> getStats() {
    return ApiResponse.success(dashboardService.getTodayStats());
}
}