package com.huayu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 错误消息发送结果类
 */
@Data
@AllArgsConstructor
public class ErrorResult {

    String errorCode;

    String errorMessage;
}
