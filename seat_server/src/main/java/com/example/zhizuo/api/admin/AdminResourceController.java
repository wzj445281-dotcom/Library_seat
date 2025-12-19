// ... (保留 imports)
package com.example.zhizuo.api.admin;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.ResourceOrder;
import com.example.zhizuo.core.entity.Resources;
import com.example.zhizuo.core.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/resources")
@Tag(name = "Admin-资源中心管理")
public class AdminResourceController {

    @Resource
    private ResourceService resourceService;

    // ... (保留 batchImport, uploadImage, add, update 等原有方法) ...

    @Operation(summary = "查询订单列表(支持状态筛选)")
    @GetMapping("/orders")
    public ApiResponse<List<ResourceOrder>> getOrders(@RequestParam(required = false) String status) {
        return ApiResponse.success(resourceService.getOrders(status));
    }

    @Operation(summary = "处理订单(发货/归还)")
    @PostMapping("/orders/{id}/process")
    public ApiResponse<String> processOrder(@PathVariable Long id, @RequestParam String action) {
        try {
            resourceService.processOrder(id, action);
            return ApiResponse.success("操作成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}