package com.huayu.webSocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天消息的类型的枚举类
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    PRIVATE(1),

    GROUP(2),

    ERROR(-1);

    private final Integer type;

    public static MessageType match(Integer type) {
        for (MessageType value : MessageType.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return ERROR;
    }
}
