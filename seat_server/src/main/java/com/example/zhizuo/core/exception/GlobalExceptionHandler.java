package com.example.zhizuo.core.exception;

import com.example.zhizuo.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        // 打印堆栈信息方便调试
        e.printStackTrace();
        // 返回友好的错误提示
        return ApiResponse.error(500, e.getMessage());
    }

    // 你可以继续添加自定义异常处理，比如 CreditTooLowException
}