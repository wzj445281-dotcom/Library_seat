package com.petsaas.core.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data // 使用 Lombok 简化 getter/setter
@Schema(description = "用户登录请求对象")
public class UserLoginDTO {

    @Schema(description = "用户名/学号", example = "2021001")
    @NotBlank(message = "用户名不能为空")
    @JsonAlias("studentId") // [修复] 允许前端传 studentId 自动映射到 username
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
    
    // 显式添加getter/setter方法以确保编译通过
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}