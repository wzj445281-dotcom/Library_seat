package com.example.zhizuo.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "用户登录请求对象")
public class UserLoginDTO {

    @Schema(description = "学号", example = "2021001")
    @NotBlank(message = "学号不能为空")
    private String studentId;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}