package com.example.zhizuo.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.zhizuo.core.entity.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService extends IService<Product> {

    // 分页查询商品
    Page<Product> getProductList(int page, int size, String keyword, Long categoryId);

    // 图片上传
    String uploadImage(MultipartFile file);
}