package com.example.zhizuo.api.admin;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.service.AdminSeatService;
import io.swagger.v3.oas.annotations.Operation; // 如果你有用到 Swagger/Knife4j
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seat")
@Tag(name = "Admin-座位管理")
public class AdminSeatController {

    private final AdminSeatService adminSeatService;

    public AdminSeatController(AdminSeatService adminSeatService) {
        this.adminSeatService = adminSeatService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Seat>> list() {
        return ApiResponse.success(adminSeatService.getAllSeats());
    }

    // --- 原有的批量添加 ---
    @PostMapping("/batch-add")
    public ApiResponse<String> batchAdd(@RequestParam int row, @RequestParam int count) {
        try {
            adminSeatService.batchCreateSeats(row, count);
            return ApiResponse.success("批量生成完成");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // --- 新增：单点添加座位 (用于精细化设计) ---
    @Operation(summary = "单独添加一个座位")
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody Seat seat) {
        // 简单校验
        if (seat.getGridX() == null || seat.getGridY() == null || seat.getLabel() == null) {
            return ApiResponse.error(400, "坐标和座位号不能为空");
        }
        boolean success = adminSeatService.save(seat);
        return success ? ApiResponse.success("添加成功") : ApiResponse.error(500, "添加失败");
    }

    // --- 新增：修改座位信息 (用于调整位置/状态/改名) ---
    @Operation(summary = "修改座位(坐标/状态/名称)")
    @PostMapping("/update")
    public ApiResponse<String> update(@RequestBody Seat seat) {
        if (seat.getId() == null) {
            return ApiResponse.error(400, "ID不能为空");
        }
        // updateById 是 MyBatis-Plus ServiceImpl 自带的方法
        boolean success = adminSeatService.updateById(seat);
        return success ? ApiResponse.success("修改成功") : ApiResponse.error(500, "修改失败");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminSeatService.removeById(id);
        return ApiResponse.success("删除成功");
    }
}