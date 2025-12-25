package com.petsaas.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(description = "用户登录请求对象")
public class UserLoginDTO {

    @Schema(description = "用户�?, example = "user123")
    @NotBlank(message = "用户名不能为�?)
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    // 手动添加getter和setter方法
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

    // 为了兼容性，添加getStudentId方法
    public String getStudentId() {
        return username;
    }
}