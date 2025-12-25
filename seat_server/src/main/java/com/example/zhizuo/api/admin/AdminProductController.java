package com.example.zhizuo.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.zhizuo.common.ApiResponse;
import com.example.zhizuo.core.entity.Product;
import com.example.zhizuo.core.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin/product")
@Tag(name = "Admin-商品管理(O2O)")
public class AdminProductController {

    @Resource
    private ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping("/list")
    public ApiResponse<Page<Product>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long categoryId) {
        return ApiResponse.success(productService.getProductList(page, size, keyword, categoryId));
    }

    @Operation(summary = "添加商品")
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody Product product) {
        product.setSales(0); // 初始销量
        if (product.getStatus() == null) product.setStatus(0); // 默认下架
        productService.save(product);
        return ApiResponse.success("添加成功");
    }

    @Operation(summary = "更新商品")
    @PostMapping("/update")
    public ApiResponse<String> update(@RequestBody Product product) {
        if (product.getId() == null) {
            return ApiResponse.error(400, "ID不能为空");
        }
        productService.updateById(product);
        return ApiResponse.success("更新成功");
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.removeById(id);
        return ApiResponse.success("删除成功");
    }

    @Operation(summary = "商品图片上传")
    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = productService.uploadImage(file);
            return ApiResponse.success(url);
        } catch (Exception e) {
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }
}