package com.huayu.command;

import com.huayu.webSocket.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处理添加好友的消息对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactMessage extends Command {

    private Integer type;

    private String target;

    private Boolean agreement;
}
