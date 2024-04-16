package com.huayu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 聊天消息发送结果类
 */
@Data
@AllArgsConstructor
public class ChatResult {

    Integer fromUserId;

    Integer receiverId;

    String content;

    String contentType;

    Boolean isPrivate;
}
