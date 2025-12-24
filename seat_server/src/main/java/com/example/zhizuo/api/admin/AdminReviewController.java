package com.example.zhizuo.api.admin; 
 
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; 
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; 
import com.example.zhizuo.common.ApiResponse; 
import com.example.zhizuo.core.entity.Review; 
import com.example.zhizuo.core.mapper.ReviewMapper; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*; 
 
/** 
 * 后台评价管理 
 * 替代原 AdminFeedbackController 
 */ 
@RestController 
@RequestMapping("/api/admin/reviews") 
public class AdminReviewController { 
 
    @Autowired 
    private ReviewMapper reviewMapper; 
 
    @GetMapping("/list") 
    public ApiResponse list(@RequestParam(defaultValue = "1") Integer page, 
                            @RequestParam(defaultValue = "10") Integer size) { 
        Page<Review> p = new Page<>(page, size); 
        QueryWrapper<Review> query = new QueryWrapper<>(); 
        query.orderByDesc("create_time"); 
        return ApiResponse.success(reviewMapper.selectPage(p, query)); 
    } 
 
    @PostMapping("/delete") 
    public ApiResponse delete(@RequestBody Review review) { 
        reviewMapper.deleteById(review.getId()); 
        return ApiResponse.success("删除成功"); 
    } 
}