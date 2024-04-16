package com.huayu.service;

import com.huayu.domain.Chatmessages;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huayu.dto.Result;

import java.util.List;

/**
 * Chatmessages服务层
 */
public interface IChatmessagesService extends IService<Chatmessages> {

    Result getChatMessage(Integer receiverId);


    Result getGroupMessage(Integer receiverId);
}
