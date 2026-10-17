package com.tcm.exam.common;

import lombok.Data;

/**
 * 统一响应体 {code, message, data}
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> unauthorized() {
        return error(401, "未登录或登录已过期");
    }

    public static <T> Result<T> forbidden() {
        return error(403, "无权限访问");
    }
}
