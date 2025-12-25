package com.example.zhizuo.api.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Resources; // 实体类名暂保持原名，实际项目中建议重构为 Product
import com.example.zhizuo.core.entity.ResourceOrder; // 同上，建议重构为 Order
import com.example.zhizuo.core.service.ResourceService;
import com.example.zhizuo.core.service.ResourceOrderService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 书籍商城核心控制器 (仿瑞幸模式)
 */
@RestController
@RequestMapping("/api/app/store")
public class StoreController {

    @Autowired
    private ResourceService productService; // 对应重构后的 ProductService

    @Autowired
    private ResourceOrderService orderService; // 对应重构后的 OrderService

    /**
     * 获取完整菜单 (分类 + 该分类下的书籍)
     * 瑞幸模式核心接口：一次性返回左侧菜单和右侧商品列表
     */
    @GetMapping("/menu")
    public ApiResponse<List<MenuVO>> getMenu() {
        // 1. 模拟获取分类 (实际应从 CategoryService 获取)
        List<CategoryMock> categories = getMockCategories();

        // 2. 获取所有上架商品
        // 注意：这里暂时使用旧的 Resources 实体，实际应更新 Entity 字段
        List<Resources> products = productService.list(new LambdaQueryWrapper<Resources>()
                .eq(Resources::getStatus, 1)); // 假设 status 1 为上架

        // 3. 组装 MenuVO (Category -> List<Product>)
        List<MenuVO> menuList = new ArrayList<>();

        // 按 category_id 分组 (假设 Resources 表中已有 category_id 字段)
        // 由于 Entity 未更新，这里仅做逻辑演示，实际需配合 Entity 变更
        Map<Long, List<Resources>> productMap = products.stream()
                .collect(Collectors.groupingBy(p -> p.getId() % 4 + 1)); // 临时 Mock：用 ID 取模模拟分类关联

        for (CategoryMock cat : categories) {
            MenuVO vo = new MenuVO();
            vo.setCategoryId(cat.getId());
            vo.setCategoryName(cat.getName());
            vo.setProducts(productMap.getOrDefault(cat.getId(), new ArrayList<>()));
            menuList.add(vo);
        }

        return ApiResponse.success(menuList);
    }

    /**
     * 创建订单
     */
    @PostMapping("/order/create")
    public ApiResponse<String> createOrder(@RequestBody OrderCreateDTO orderDTO) {
        // 1. 校验库存 (建议使用 Redis/Redisson)
        // 2. 计算总价
        // 3. 生成订单 (状态: PENDING_PAY)
        // 4. 发送死信队列 (15分钟未支付自动取消)

        // 模拟返回订单号
        return ApiResponse.success("ORDER_" + System.currentTimeMillis());
    }

    // --- Inner DTO/VO Classes ---

    @Data
    public static class MenuVO {
        private Long categoryId;
        private String categoryName;
        private List<Resources> products;
    }

    @Data
    public static class CategoryMock {
        private Long id;
        private String name;
        public CategoryMock(Long id, String name) { this.id = id; this.name = name; }
    }

    @Data
    public static class OrderCreateDTO {
        private List<CartItem> items;
        private Integer type; // 1 自取 2 外卖
    }

    @Data
    public static class CartItem {
        private Long productId;
        private Integer quantity;
    }

    // 简单的 Mock 分类数据辅助方法
    private List<CategoryMock> getMockCategories() {
        List<CategoryMock> list = new ArrayList<>();
        list.add(new CategoryMock(1L, "本周热销"));
        list.add(new CategoryMock(2L, "编程技术"));
        list.add(new CategoryMock(3L, "文学小说"));
        list.add(new CategoryMock(4L, "商业管理"));
        return list;
    }
}