package com.huayu.command;

import com.huayu.webSocket.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于聊天的消息对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage extends Command {

    private Integer type;

    private String target;

    private String content;
}
