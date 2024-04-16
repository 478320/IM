package com.huayu.webSocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 所有指令的父类
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Command {

    private Integer code;

    private String token;
}
