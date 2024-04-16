package com.huayu.listener;

import cn.hutool.json.JSONUtil;
import com.huayu.domain.Chatmessages;
import com.huayu.mapper.ChatmessagesMapper;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.huayu.utils.MQConstants.*;

/**
 * MQ的消息监听器
 */
@Component
public class MQListener {

    @Autowired
    private ChatmessagesMapper chatmessagesMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 监听聊天保存消息队列，异步保存聊天的消息
     *
     * @param chatmessagesJson json格式的聊天信息
     * @param messageId        消息的唯一Id用于极大概率保证消息的幂等性
     * @throws InterruptedException 对应的异常
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = CHAT_SAVE_QUEUE,
                    durable = "true",
                    arguments = @Argument(name = "x-queue-mode",
                            value = "lazy")),
            exchange = @Exchange(name = CHAT_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = {CHAT_SAVE_KEY}
    ))
    public void listenSimpleQueueMessage(String chatmessagesJson, @Header(AmqpHeaders.MESSAGE_ID) String messageId) throws InterruptedException {
        //从redis中获取消息的Id
        String s = stringRedisTemplate.opsForValue().get(messageId);
        if ("1".equals(s)) {
            //如果redis中已经存在该消息则直接返回不做处理
            return;
        }
        Chatmessages chatmessages = JSONUtil.toBean(chatmessagesJson, Chatmessages.class);
        chatmessagesMapper.insert(chatmessages);
        //向redis中保存该消息的Id信息
        stringRedisTemplate.opsForValue().set(messageId, "1", 10, TimeUnit.SECONDS);
    }
}
