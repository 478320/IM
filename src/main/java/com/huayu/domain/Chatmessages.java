package com.huayu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_chatmessages")
public class Chatmessages implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "message_Id", type = IdType.AUTO)
    private Long messageId;

    private Integer senderId;

    private Integer receiverId;

    private String content;

    private String messageType;

    private LocalDateTime timestamp;

    private String status;

}
