package com.petsaas.common.api;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一API响应结果封装
 * @param <T> 数据载体类型
 */
@Data
public class Result<T> implements Serializable {
    private long code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failed(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> failed(long code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
