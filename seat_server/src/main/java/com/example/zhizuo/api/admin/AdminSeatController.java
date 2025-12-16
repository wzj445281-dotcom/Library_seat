package com.example.zhizuo.api.admin;

import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Seat;
import com.example.zhizuo.core.service.AdminSeatService; // 引用接口，不引用实现类
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seat")
public class AdminSeatController {

    // 依赖注入的是 Service，不再是 Mapper
    private final AdminSeatService adminSeatService;

    public AdminSeatController(AdminSeatService adminSeatService) {
        this.adminSeatService = adminSeatService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Seat>> list() {
        return ApiResponse.success(adminSeatService.getAllSeats());
    }

    @PostMapping("/batch-add")
    public ApiResponse<String> batchAdd(@RequestParam int row, @RequestParam int count) {
        try {
            adminSeatService.batchCreateSeats(row, count);
            return ApiResponse.success("批量生成完成");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 其他删除、修改状态的方法，直接调用 adminSeatService.removeById() 等 MyBatis Plus 自带方法
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminSeatService.removeById(id);
        return ApiResponse.success("删除成功");
    }
}