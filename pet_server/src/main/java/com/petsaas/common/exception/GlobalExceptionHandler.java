package com.petsaas.common.exception;

import com.petsaas.common.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 拦截所有未捕获异常，保证前端收到的永远是 JSON 格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 兜底处理所有未知异常
     */
    @ExceptionHandler(value = Exception.class)
    public Result<String> handleException(Exception e) {
        // 打印堆栈信息到日志，方便排查
        log.error("系统内部异常: ", e);
        // 返回友好的错误提示给前端
        return Result.failed("系统繁忙，请稍后重试");
    }

    // 示例：如果你有自定义的业务异常 (BusinessException)，可以取消注释并启用
    /*
    @ExceptionHandler(value = BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.failed(e.getCode(), e.getMessage());
    }
    */
}