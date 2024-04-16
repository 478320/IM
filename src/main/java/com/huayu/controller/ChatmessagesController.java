package com.huayu.controller;


import com.huayu.dto.Result;
import com.huayu.service.IChatmessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * chatmessages表现层对象
 */
@RestController
@RequestMapping("/chatmessages")
public class ChatmessagesController {

    @Autowired
    private IChatmessagesService chatmessagesService;

    /**
     * 查询和某个用户的三天内的聊天记录
     *
     * @param receiverId 要查询的用户的ID
     * @return 查询到的消息
     */
    @GetMapping("/get/one/{receiverId}")
    public Result getChatMessage(@PathVariable("receiverId") Integer receiverId) {
        return chatmessagesService.getChatMessage(receiverId);
    }

    /**
     * 查询某个群三天内的聊天记录
     *
     * @param receiverId 要查询的群的ID
     * @return 查询到的消息
     */
    @GetMapping("/get/group/{receiverId}")
    public Result getGroupMessage(@PathVariable("receiverId") Integer receiverId) {
        return chatmessagesService.getGroupMessage(receiverId);
    }

}
