# 用户ID获取问题修复说明

## 问题分析

错误信息：`获取当前用户ID失败: 无法识别的用户信息类型: java.lang.String`

**根本原因**：
- `UserDetailsServiceImpl.loadUserByUsername()` 返回的是 Spring Security 的 `org.springframework.security.core.userdetails.User` 对象
- `SecurityUtils.getUserId()` 期望的是自定义的 `User` 实体对象（有 `getId()` 方法）
- 当 principal 是 String 类型时，无法直接获取用户ID

## 已完成的修复

### 1. 修改 UserDetailsServiceImpl ✅

**文件**：`seat_server/src/main/java/com/example/zhizuo/core/impl/UserDetailsServiceImpl.java`

**修改内容**：
- 创建了 `UserDetailsWrapper` 类，将 `User` 实体包装为 `UserDetails`
- 修改 `loadUserByUsername()` 方法，返回 `UserDetailsWrapper` 而不是 Spring Security 的 `User`
- 支持通过 `username` 或 `phone` 查询用户

**关键代码**：
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    QueryWrapper<User> query = new QueryWrapper<>();
    // 支持通过 username 或 phone 查询
    query.and(wrapper -> wrapper.eq("username", username).or().eq("phone", username));
    User user = userMapper.selectOne(query);
    
    if (user == null) {
        throw new UsernameNotFoundException("用户不存在: " + username);
    }
    
    // 返回包装类，包含 User 实体
    return new UserDetailsWrapper(user);
}
```

### 2. 修改 SecurityUtils ✅

**文件**：`seat_server/src/main/java/com/example/zhizuo/common/util/SecurityUtils.java`

**修改内容**：
- 添加了对 `UserDetailsWrapper` 的识别和处理
- 可以从 `UserDetailsWrapper` 中获取 `User` 实体，然后获取 ID

**关键代码**：
```java
// 情况2: 如果是 UserDetailsWrapper (我们自定义的包装类)
if (principal instanceof com.example.zhizuo.core.impl.UserDetailsServiceImpl.UserDetailsWrapper) {
    com.example.zhizuo.core.impl.UserDetailsServiceImpl.UserDetailsWrapper wrapper = 
        (com.example.zhizuo.core.impl.UserDetailsServiceImpl.UserDetailsWrapper) principal;
    return wrapper.getUser().getId();
}
```

## 需要您执行的操作

### 重启后端服务 ⚠️ 重要

**必须重启后端服务**，代码更改才能生效：

1. 停止当前运行的后端服务（如果正在运行）
   - 在终端按 `Ctrl+C`
   - 或在 IDE 中停止 Spring Boot 应用

2. 重新启动后端服务
   ```bash
   cd seat_server
   mvn spring-boot:run
   ```

3. 重新登录（如果需要）
   - 由于认证逻辑已更改，建议重新登录获取新的 Token
   - 或者清除小程序缓存后重新登录

4. 测试地址列表功能
   - 在小程序中进入"地址管理"页面
   - 应该能正常加载地址列表，不再报错

## 验证清单

- [ ] 后端服务已重启
- [ ] 用户已重新登录（获取新的 Token）
- [ ] 地址列表页面能正常加载
- [ ] 其他需要用户ID的功能正常工作

## 技术说明

### UserDetailsWrapper 类

这个包装类实现了 `UserDetails` 接口，同时保留了 `User` 实体的引用：

```java
public static class UserDetailsWrapper implements UserDetails {
    private final User user;
    
    public User getUser() {
        return user;
    }
    
    // 实现 UserDetails 接口的所有方法
    // ...
}
```

### 工作流程

1. **JWT Filter** 从 Token 中提取 username
2. **UserDetailsServiceImpl** 通过 username 查询数据库，获取 `User` 实体
3. **UserDetailsServiceImpl** 将 `User` 包装为 `UserDetailsWrapper` 返回
4. **JWT Filter** 将 `UserDetailsWrapper` 放入 SecurityContext
5. **SecurityUtils.getUserId()** 从 `UserDetailsWrapper` 中提取 `User` 实体，然后获取 ID

## 常见问题

### Q1: 重启后仍然报错
- 确认代码已正确编译
- 检查后端日志，查看是否有编译错误
- 确认用户已重新登录

### Q2: 其他功能也报类似错误
- 所有使用 `SecurityUtils.getUserId()` 的地方都应该能正常工作
- 如果还有问题，检查是否还有其他地方需要修改

### Q3: 登录失败
- 检查数据库中的用户数据
- 确认 username 或 phone 字段有值
- 查看后端日志，查看具体错误信息

---

**重要提示**：代码更改后，**必须重启后端服务**才能生效！

