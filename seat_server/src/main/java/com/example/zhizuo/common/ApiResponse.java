package com.example.zhizuo.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 通用接口响应包装类
 * @param <T> 数据类型
 */
@Data
public class ApiResponse<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    // 构造函数
    public ApiResponse() {}

    public ApiResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应 (带数据)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    // 成功响应 (无数据)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }

    // 错误响应
    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(500, msg, null);
    }

    // 错误响应 (自定义状态码)
    public static <T> ApiResponse<T> error(Integer code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}