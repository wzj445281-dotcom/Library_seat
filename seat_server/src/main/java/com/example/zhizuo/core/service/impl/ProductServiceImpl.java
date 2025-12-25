package com.example.zhizuo.core.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.mapper.ProductMapper;
import com.example.zhizuo.core.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Override
    public Page<Product> getProductList(int page, int size, String keyword, Long categoryId) {
        Page<Product> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 动态条件
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(Product::getCreateTime);
        return this.page(pageInfo, wrapper);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String newFileName = IdUtil.simpleUUID() + "." + suffix;

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }

        // 返回访问路径 (假设 WebMvcConfig 已配置 /uploads/** 映射)
        return "/uploads/" + newFileName;
    }
}