package com.huayu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Websocket返回状态对象
 */
@Data
@AllArgsConstructor
public class Status {

    private int code;

    private String message;
}
