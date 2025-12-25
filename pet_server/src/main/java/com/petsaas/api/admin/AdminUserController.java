package com.petsaas.api.admin; 
 
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; 
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; 
import com.petsaas.common.ApiResponse; 
import com.petsaas.core.dto.UserRechargeDTO; 
import com.petsaas.core.entity.TransactionFlow; 
import com.petsaas.core.entity.User; 
import com.petsaas.core.mapper.TransactionMapper; 
import com.petsaas.core.mapper.UserMapper; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.validation.annotation.Validated; 
import org.springframework.web.bind.annotation.*; 
 
import java.math.BigDecimal; 
import java.time.LocalDateTime; 
 
/** 
 * 后台用户管理 (升级版：带校验与流水) 
 */ 
@RestController 
@RequestMapping("/api/admin/users") 
public class AdminUserController { 
 
    @Autowired 
    private UserMapper userMapper; 
 
    @Autowired 
    private TransactionMapper transactionMapper; 
 
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
            query.like("name", username).or().like("student_id", username); 
        } 
        return ApiResponse.success(userMapper.selectPage(userPage, query)); 
    } 
 
    // 管理员充值余额 (带事务与流水) 
    @PostMapping("/recharge") 
    @Transactional(rollbackFor = Exception.class) 
    public ApiResponse recharge(@RequestBody @Validated UserRechargeDTO payload) { 
        User user = userMapper.selectById(payload.getUserId()); 
        if (user == null) { 
            return ApiResponse.error("用户不存在"); 
        } 
 
        // 1. 更新余额 
        user.setBalance(user.getBalance().add(payload.getAmount())); 
        userMapper.updateById(user); 
 
        // 2. 记录流水 
        TransactionFlow flow = TransactionFlow.builder() 
                .userId(user.getId()) 
                .amount(payload.getAmount()) 
                .type("RECHARGE") 
                .description("管理员后台充值") 
                .createTime(LocalDateTime.now()) 
                .build(); 
        transactionMapper.insert(flow); 
 
        return ApiResponse.success("充值成功，当前余额: " + user.getBalance()); 
    } 
 
    // 重置密码 
    @PostMapping("/reset-pwd") 
    public ApiResponse resetPwd(@RequestBody UserRechargeDTO payload) { // 复用DTO中的userId 
        User user = userMapper.selectById(payload.getUserId()); 
        if (user != null) { 
            user.setPassword(passwordEncoder.encode("123456")); 
            userMapper.updateById(user); 
            return ApiResponse.success("密码已重置为 123456"); 
        } 
        return ApiResponse.error("用户不存在"); 
    } 
}