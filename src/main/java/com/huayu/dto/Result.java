package com.huayu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据的返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Boolean success;
    private String msg;
    private String errorMsg;
    private Object data;
    private Integer total;

    public static Result ok() {
        return new Result(true, null, null, null, null);
    }

    public static Result ok(Object data) {
        return new Result(true, null, null, data, null);
    }

    public static Result ok(String msg, Object data) {
        return new Result(true, msg, null, data, null);
    }

    public static Result ok(List<?> data, Integer total) {
        return new Result(true, null, null, data, total);
    }

    public static Result ok(String msg) {
        return new Result(true, msg, null, null, null);
    }


    public static Result fail(String errorMsg) {
        return new Result(false, null, errorMsg, null, null);
    }
}
