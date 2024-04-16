package com.huayu.webSocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友请求的消息的枚举类
 */
@Getter
@AllArgsConstructor
public enum ContactMessageType {

    SEND(1),

    CONFIRM(2),

    ERROR(-1);

    private final Integer type;

    public static ContactMessageType match(Integer type) {
        for (ContactMessageType value : ContactMessageType.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return ERROR;
    }
}
