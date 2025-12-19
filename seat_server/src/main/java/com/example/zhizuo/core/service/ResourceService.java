package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.ResourceOrder;
import com.example.zhizuo.core.entity.Resources;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService extends IService<Resources> {

    // ================= 管理端 =================
    int batchImport(MultipartFile file);

    // 查询订单（支持状态筛选）
    List<ResourceOrder> getOrders(String status);

    // 处理订单状态（发货/归还）
    void processOrder(Long orderId, String action);

    // ================= 客户端 =================
    String borrow(Long userId, Long resourceId, Integer deliveryType);
    void addToCart(Long userId, Long resourceId);
    void removeFromCart(Long userId, Long resourceId);
    List<Resources> getCartList(Long userId);
    List<String> submitCart(Long userId, Integer deliveryType);
}