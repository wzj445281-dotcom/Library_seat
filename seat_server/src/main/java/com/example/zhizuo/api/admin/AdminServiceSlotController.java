package com.example.zhizuo.api.admin;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.ServiceSlot;
import com.example.zhizuo.core.service.AdminServiceSlotService;
import io.swagger.v3.oas.annotations.Operation; // 如果你有用到 Swagger/Knife4j
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/service-slot")
@Tag(name = "Admin-服务工位管理")
public class AdminServiceSlotController {

    private final AdminServiceSlotService adminServiceSlotService;

    public AdminServiceSlotController(AdminServiceSlotService adminServiceSlotService) {
        this.adminServiceSlotService = adminServiceSlotService;
    }

    @GetMapping("/list")
    public ApiResponse<List<ServiceSlot>> list() {
        return ApiResponse.success(adminServiceSlotService.getAllServiceSlots());
    }

    // --- 原有的批量添加 ---
    @PostMapping("/batch-add")
    public ApiResponse<String> batchAdd(@RequestParam int count) {
        try {
            adminServiceSlotService.batchCreateServiceSlots(count);
            return ApiResponse.success("批量生成完成");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // --- 新增：单点添加工位 (用于精细化设计) ---
    @Operation(summary = "单独添加一个服务工位")
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody ServiceSlot serviceSlot) {
        // 简单校验
        if (serviceSlot.getName() == null || serviceSlot.getServiceType() == null || serviceSlot.getPrice() == null) {
            return ApiResponse.error(400, "工位名称、服务类型和价格不能为空");
        }
        boolean success = adminServiceSlotService.save(serviceSlot);
        return success ? ApiResponse.success("添加成功") : ApiResponse.error(500, "添加失败");
    }

    // --- 新增：修改工位信息 (用于调整价格/状态/类型) ---
    @Operation(summary = "修改服务工位(名称/类型/价格/状态)")
    @PostMapping("/update")
    public ApiResponse<String> update(@RequestBody ServiceSlot serviceSlot) {
        if (serviceSlot.getId() == null) {
            return ApiResponse.error(400, "ID不能为空");
        }
        // updateById 是 MyBatis-Plus ServiceImpl 自带的方法
        boolean success = adminServiceSlotService.updateById(serviceSlot);
        return success ? ApiResponse.success("修改成功") : ApiResponse.error(500, "修改失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminServiceSlotService.removeById(id);
        return ApiResponse.success("删除成功");
    }
}