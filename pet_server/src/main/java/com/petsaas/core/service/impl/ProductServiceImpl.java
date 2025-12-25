package com.petsaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petsaas.common.util.SecurityUtils;
import com.petsaas.core.entity.Product;
import com.petsaas.core.entity.User;
import com.petsaas.core.mapper.ProductMapper;
import com.petsaas.core.mapper.UserMapper;
import com.petsaas.core.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Product> getProductList(String category) {
        QueryWrapper<Product> query = new QueryWrapper<>();
        query.eq("status", 1); // 只显示上架商�?        if (category != null && !category.isEmpty()) {
            query.eq("category", category);
        }
        return productMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyProduct(Long userId, Long productId, Integer quantity) {
        // 1. 检查商品和库存
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下�?);
        }
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        // 2. 检查用户余�?        User user = userMapper.selectById(userId);
        BigDecimal totalCost = product.getPrice().multiply(new BigDecimal(quantity));
        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("余额不足，请充�?);
        }

        // 3. 扣减库存 & 扣减余额
        int stockRows = productMapper.deductStock(productId, quantity);
        if (stockRows == 0) throw new RuntimeException("库存并发扣减失败，请重试");
        
        int balanceRows = userMapper.deductBalance(userId, totalCost);
        if (balanceRows == 0) throw new RuntimeException("余额扣减失败");
        
        // (可�? 这里应该插入订单记录 OrderRecord，为了简化演示暂省略
    }
}