package com.petsaas.api.admin; 
 
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; 
import com.petsaas.common.ApiResponse; 
import com.petsaas.core.entity.ServiceSlot; 
import com.petsaas.core.mapper.ServiceSlotMapper; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*; 
 
import java.util.List; 
 
/** 
 * 后台工位管理接口 
 */ 
@RestController 
@RequestMapping("/api/admin/slots") 
public class AdminServiceSlotController { 
 
    @Autowired 
    private ServiceSlotMapper serviceSlotMapper; 
 
    // 获取所有工�?
    @GetMapping("/list") 
    public ApiResponse list() { 
        return ApiResponse.success(serviceSlotMapper.selectList(null)); 
    } 
 
    // 新增/修改工位 
    @PostMapping("/save") 
    public ApiResponse save(@RequestBody ServiceSlot slot) { 
        if (slot.getId() == null) { 
            serviceSlotMapper.insert(slot); 
        } else { 
            serviceSlotMapper.updateById(slot); 
        } 
        return ApiResponse.success("保存成功"); 
    } 
 
    // 删除工位 
    @PostMapping("/delete") 
    public ApiResponse delete(@RequestBody ServiceSlot slot) { 
        serviceSlotMapper.deleteById(slot.getId()); 
        return ApiResponse.success("删除成功"); 
    } 
}