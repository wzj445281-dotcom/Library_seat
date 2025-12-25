package com.petsaas.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petsaas.core.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {
    // 获取商品列表
    List<Product> getProductList(String category);
    
    // 购买商品
    void buyProduct(Long userId, Long productId, Integer quantity);
}