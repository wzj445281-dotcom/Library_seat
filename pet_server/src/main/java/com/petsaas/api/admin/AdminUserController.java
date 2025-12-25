package com.petsaas.api.admin; 
 
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; 
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; 
import com.petsaas.common.ApiResponse; 
import com.petsaas.core.entity.User; 
import com.petsaas.core.mapper.UserMapper; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.web.bind.annotation.*; 
 
import java.math.BigDecimal; 
import java.util.Map; 
 
/** 
 * 后台用户管理 
 */ 
@RestController 
@RequestMapping("/api/admin/users") 
public class AdminUserController { 
 
    @Autowired 
    private UserMapper userMapper; 
    
    @Autowired 
    private PasswordEncoder passwordEncoder; 
 
    // 用户列表 
    @GetMapping("/list") 
    public ApiResponse list(@RequestParam(defaultValue = "1") Integer page, 
                            @RequestParam(defaultValue = "10") Integer size, 
                            @RequestParam(required = false) String username) { 
        Page<User> userPage = new Page<>(page, size); 
        QueryWrapper<User> query = new QueryWrapper<>(); 
        if (username != null && !username.isEmpty()) { 
            query.like("username", username); 
        } 
        return ApiResponse.success(userMapper.selectPage(userPage, query)); 
    } 
 
    // 更新用户信息 (如积分、角�? 
    @PostMapping("/update") 
    public ApiResponse update(@RequestBody User user) { 
        User existing = userMapper.selectById(user.getId()); 
        if (existing == null) return ApiResponse.error("用户不存�?); 
 
        if (user.getPoints() != null) existing.setPoints(user.getPoints()); 
        if (user.getRole() != null) existing.setRole(user.getRole()); 
        if (user.getName() != null) existing.setName(user.getName()); 
        
        userMapper.updateById(existing); 
        return ApiResponse.success("更新成功"); 
    } 
 
    // 管理员充值余�?
    @PostMapping("/recharge") 
    public ApiResponse recharge(@RequestBody Map<String, Object> payload) { 
        Long userId = Long.valueOf(payload.get("userId").toString()); 
        BigDecimal amount = new BigDecimal(payload.get("amount").toString()); 
        
        User user = userMapper.selectById(userId); 
        if (user != null) { 
            user.setBalance(user.getBalance().add(amount)); 
            userMapper.updateById(user); 
            return ApiResponse.success("充值成功，当前余额: " + user.getBalance()); 
        } 
        return ApiResponse.error("用户不存�?); 
    } 
    
    // 重置密码 
    @PostMapping("/reset-pwd") 
    public ApiResponse resetPwd(@RequestBody Map<String, Long> payload) { 
        Long userId = payload.get("userId"); 
        User user = userMapper.selectById(userId); 
        if (user != null) { 
            user.setPassword(passwordEncoder.encode("123456")); 
            userMapper.updateById(user); 
            return ApiResponse.success("密码已重置为 123456"); 
        } 
        return ApiResponse.error("用户不存�?); 
    } 
}