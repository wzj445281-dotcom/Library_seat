package com.petsaas.api.admin; 
 
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; 
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; 
import com.petsaas.common.ApiResponse; 
import com.petsaas.core.entity.Product; 
import com.petsaas.core.service.ProductService; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*; 
 
/** 
 * 后台商品管理接口 
 */ 
@RestController 
@RequestMapping("/api/admin/products") 
public class AdminProductController { 
 
    @Autowired 
    private ProductService productService; 
 
    // 分页查询商品 
    @GetMapping("/list") 
    public ApiResponse list(@RequestParam(defaultValue = "1") Integer page, 
                            @RequestParam(defaultValue = "10") Integer size, 
                            @RequestParam(required = false) String name) { 
        Page<Product> productPage = new Page<>(page, size); 
        QueryWrapper<Product> query = new QueryWrapper<>(); 
        if (name != null && !name.isEmpty()) { 
            query.like("name", name); 
        } 
        query.orderByDesc("create_time"); 
        return ApiResponse.success(productService.page(productPage, query)); 
    } 
 
    // 新增或更新商�?
    @PostMapping("/save") 
    public ApiResponse save(@RequestBody Product product) { 
        boolean success = productService.saveOrUpdate(product); 
        return success ? ApiResponse.success("保存成功") : ApiResponse.error("保存失败"); 
    } 
 
    // 删除商品 
    @PostMapping("/delete") 
    public ApiResponse delete(@RequestBody Product product) { 
        productService.removeById(product.getId()); 
        return ApiResponse.success("删除成功"); 
    } 
 
    // 上架/下架 
    @PostMapping("/status") 
    public ApiResponse updateStatus(@RequestBody Product product) { 
        Product p = new Product(); 
        p.setId(product.getId()); 
        p.setStatus(product.getStatus()); // 1上架 0下架 
        productService.updateById(p); 
        return ApiResponse.success("状态更新成�?); 
    } 
}