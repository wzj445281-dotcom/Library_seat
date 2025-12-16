package com.example.zhizuo.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/list")
    public ApiResponse<Page<User>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String studentId) {
        return ApiResponse.success(adminUserService.getUserList(page, size, studentId));
    }

    @PostMapping("/reset-credit")
    public ApiResponse<String> resetCredit(@RequestParam Long userId) {
        try {
            adminUserService.resetCredit(userId);
            return ApiResponse.success("信用分重置成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}