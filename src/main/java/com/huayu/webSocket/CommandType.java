package com.huayu.webSocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 指令code的枚举类
 */
@Getter
@AllArgsConstructor
public enum CommandType {

    CONNECTION(10001),

    CHAT(10002),

    CONTACT(10003),

    EXIT(10004),

    ERROR(-1);

    private final Integer type;

    public static CommandType match(Integer type) {
        for (CommandType value : CommandType.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return ERROR;
    }
}
