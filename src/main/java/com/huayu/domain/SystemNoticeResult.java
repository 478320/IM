package com.huayu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 系统消息发送结果类
 */
@Data
@AllArgsConstructor
public class SystemNoticeResult {

    String message;

    Boolean actionRequired;

}
