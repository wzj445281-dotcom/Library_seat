package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.ResourceOrder;
import com.example.zhizuo.core.entity.Resources;
import com.example.zhizuo.core.entity.User;
import com.example.zhizuo.core.mapper.ResourcesMapper;
import com.example.zhizuo.core.mapper.UserMapper;
import com.example.zhizuo.core.service.ResourceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * O2O 物资流转控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/app/resources")
@Tag(name = "App-物资借用")
public class AppResourceController {

    @Resource
    private ResourceOrderService resourceOrderService;
    @Resource
    private ResourcesMapper resourcesMapper;
    @Resource
    private UserMapper userMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = (String) auth.getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("student_id", studentId));
        return user.getId();
    }

    @Operation(summary = "获取物资列表")
    @GetMapping("/list")
    public ApiResponse<List<Resources>> getList(@RequestParam(required = false) Integer type) {
        QueryWrapper<Resources> query = new QueryWrapper<>();
        query.eq("status", 1); // 只看上架的
        if (type != null) {
            query.eq("type", type);
        }
        return ApiResponse.success(resourcesMapper.selectList(query));
    }

    @Operation(summary = "创建借用订单")
    @PostMapping("/borrow")
    public ApiResponse<String> borrow(@RequestBody Map<String, Object> params) {
        Long resourceId = Long.valueOf(params.get("resourceId").toString());
        Integer deliveryType = Integer.valueOf(params.get("deliveryType").toString());

        try {
            String orderNo = resourceOrderService.createOrder(getCurrentUserId(), resourceId, deliveryType);
            return ApiResponse.success(orderNo);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @Operation(summary = "归还物资")
    @PostMapping("/return")
    public ApiResponse<String> returnResource(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        try {
            resourceOrderService.returnResource(orderNo);
            return ApiResponse.success("归还成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @Operation(summary = "我的借用记录")
    @GetMapping("/my-orders")
    public ApiResponse<Page<ResourceOrder>> myOrders(@RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        Page<ResourceOrder> orderPage = new Page<>(page, size);
        QueryWrapper<ResourceOrder> query = new QueryWrapper<>();
        query.eq("user_id", getCurrentUserId());
        query.orderByDesc("create_time");

        // 注意：ResourceOrderService 继承了 ServiceImpl，可以直接调用 page 方法
        // 如果 IDE 报错，请确保 ResourceOrderService 继承了 IService<ResourceOrder>
        // 这里假设 resourceOrderService 已经正确继承
        return ApiResponse.success((Page<ResourceOrder>) resourceOrderService.page(orderPage, query));
    }
}