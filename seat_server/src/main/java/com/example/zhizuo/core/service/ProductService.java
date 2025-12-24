package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.ShopOrder;
import com.example.zhizuo.core.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService extends IService<Product> {

    // ================= 管理端 =================
    int batchImport(MultipartFile file);

    // 查询订单（支持状态筛选）
    List<ShopOrder> getOrders(String status);

    // 处理订单状态（发货/完成）
    void processOrder(Long orderId, String action);

    // ================= 客户端 =================
    String purchase(Long userId, Long productId, Integer quantity);
    void addToCart(Long userId, Long productId);
    void removeFromCart(Long userId, Long productId);
    List<Product> getCartList(Long userId);
    List<String> submitCart(Long userId);
    
    // 获取热销商品（用于AI推荐）
    List<Product> getTopSellingProducts(int limit);
}