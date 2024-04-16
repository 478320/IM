package com.huayu.handler;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huayu.Session.SessionFactory;
import com.huayu.command.ChatMessage;
import com.huayu.domain.*;
import com.huayu.dto.WebSocketResult;
import com.huayu.mapper.ContactMapper;
import com.huayu.mapper.GroupMembersMapper;
import com.huayu.mapper.UserMapper;
import com.huayu.utils.AnalysisUtilWebsocket;
import com.huayu.utils.MQConstants;
import com.huayu.utils.RedisConstans;
import com.huayu.utils.SpringContextUtil;
import com.huayu.webSocket.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.StringUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.huayu.utils.RedisConstans.*;
import static com.huayu.utils.WebSocketErrorConstants.*;

/**
 * 聊天处理器
 */
public class ChatHandler {

    /**
     * handler执行方法
     *
     * @param channelHandlerContext 用户的ctx
     * @param textWebSocketFrame    用户传递信息的载体
     */
    public static void execute(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) {
        try {
            ChatMessage chat = JSONUtil.toBean(textWebSocketFrame.text(), ChatMessage.class);
            //获取要聊天的用户名
            String target = chat.getTarget();
            StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
            GroupMembersMapper groupMembersMapper = SpringContextUtil.getBean(GroupMembersMapper.class);
            UserMapper userMapper = SpringContextUtil.getBean(UserMapper.class);
            RabbitTemplate rabbitTemplate = SpringContextUtil.getBean(RabbitTemplate.class);
            //根据消息的type字段判段如果是1则走私聊
            switch (MessageType.match(chat.getType())) {
                case PRIVATE -> {
                    //尝试从redis中获取到要发送给的用户的channelId
                    String s = stringRedisTemplate.opsForValue().get(USER_CHANNEL_KEY + target);
                    //将传来的token分析处用户的Id
                    Integer fromUserId = AnalysisUtilWebsocket.analysisTokenToUserId(chat.getToken());
                    //判断用户是否选择了发送人
                    if (StringUtil.isNullOrEmpty(target)) {
                        //未选择联系人发送系统消息告知用户
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_POINT_ERR, "消息发送失败，发送消息前请指定发送对象"), Instant.now().toString()));
                        return;
                    }
                    //根据用户名查询到用户的ID这是最早期设计上的失误，待改善
                    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(User::getUsername, chat.getTarget());
                    Integer receiverId = userMapper.selectOne(queryWrapper).getId();
                    //判断发送人是否是消息接收人的好友
                    if (isBlock(fromUserId, receiverId) == null) {
                        //没有获取到屏蔽信息说明两个用户还不是好友，给出对应提示信息
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("notification", new SystemNoticeResult("消息发送失败，您还不是对方的好友", false), Instant.now().toString()));
                        return;
                    }
                    //判断消息接收人是否屏蔽了用户
                    if (isBlock(fromUserId, receiverId)) {
                        //屏蔽了用户则给出对应的系统提示
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("notification", new SystemNoticeResult("消息发送失败，对方已将你屏蔽", false), Instant.now().toString()));
                        return;
                    }
                    //判断发送人是否在线
                    if (s == null) {
                        //用户如果不在线，向MQ发送消息，由MQ异步将消息保存到数据库中
                        Chatmessages msg = createChatMessage(chat, receiverId, "单聊", fromUserId, "3");
                        rabbitTemplate.convertAndSend(MQConstants.CHAT_EXCHANGE, MQConstants.CHAT_SAVE_KEY, JSONUtil.toJsonPrettyStr(msg));
                    } else {
                        //用户如果在线，则封装数据库消息信息
                        Chatmessages msg = createChatMessage(chat, receiverId, "单聊", fromUserId, "2");
                        //删除用户聊天消息的缓存，采取先删缓存后操作数据库的主动更新策略，保证一致性
                        stringRedisTemplate.delete(CACHE_ONE_CHAT_KEY + fromUserId + ":" + receiverId);
                        //向rabbitMQ发送消息异步将消息保存到数据库中保证发送消息的高效性
                        rabbitTemplate.convertAndSend(MQConstants.CHAT_EXCHANGE, MQConstants.CHAT_SAVE_KEY, JSONUtil.toJsonPrettyStr(msg));
                        //发送消息给用户
                        Channel channel = SessionFactory.getSession().getChannel(s);
                        channel.writeAndFlush(WebSocketResult.ok("message", new ChatResult(fromUserId, receiverId, "text", chat.getContent(), true), Instant.now().toString()));
                    }
                }
                //根据消息的type字段判段如果是2则走群聊
                case GROUP -> {
                    //判断用户是否指定了发送的群聊
                    Integer fromUserId = AnalysisUtilWebsocket.analysisTokenToUserId(chat.getToken());
                    if (StringUtil.isNullOrEmpty(target)) {
                        //如果未指定对应的群聊则发送系统消息提示用户
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_POINT_ERR, "消息发送失败，请指定要发送消息的群"), Instant.now().toString()));
                        return;
                    }
                    //根据target（群聊ID）查找到对应群成员
                    LambdaQueryWrapper<GroupMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper.eq(GroupMembers::getGroupId, target);
                    List<GroupMembers> groupMembers = groupMembersMapper.selectList(lambdaQueryWrapper);
                    // 做一个判断用户是否属于群聊的判断标识
                    boolean isBelong = groupMembers.stream().anyMatch(groupMembers1 -> groupMembers1.getUserId().equals(fromUserId));
                    //判断用户是否属于群聊
                    if (!isBelong) {
                        //用户不属于群聊则发消息给出用户提示
                        channelHandlerContext.writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_BELONG_ERR, "您还不是群成员无法发送群消息"), Instant.now().toString()));
                    }
                    //是群成员则获取所有群成员的channelID
                    List<String> channelIdList = groupMembers.stream().map(groupMembers1 -> {
                        User user = userMapper.selectById(groupMembers1.getUserId());
                        String s = stringRedisTemplate.opsForValue().get(USER_CHANNEL_KEY + user.getUsername());
                        return s;
                    }).collect(Collectors.toList());
                    channelIdList.forEach(s -> {
                        //向所有在线的群成员发送消息
                        if (s != null) {
                            //正常发送消息
                            Channel channel = SessionFactory.getSession().getChannel(s);
                            channel.writeAndFlush(WebSocketResult.ok("message", new ChatResult(fromUserId, Integer.parseInt(target), chat.getContent(), "text", false), Instant.now().toString()));
                        }
                    });
                    //对所有成员的消息，封装数据库存储对象
                    Chatmessages chatmessages = new Chatmessages();
                    chatmessages.setSenderId(fromUserId).setReceiverId(Integer.parseInt(chat.getTarget())).setContent(chat.getContent()).setMessageType("群聊").setTimestamp(LocalDateTime.now()).setStatus("2");
                    //删除群消息的缓存，缓存采取先删缓存后操作数据库的主动更新策略，保证一致性
                    stringRedisTemplate.delete(RedisConstans.CACHE_GROUP_CHAT_KEY + target);
                    //向rabbitMQ发送消息，异步将消息同步到数据库中，保证聊天的高效性
                    rabbitTemplate.convertAndSend(MQConstants.CHAT_EXCHANGE, MQConstants.CHAT_SAVE_KEY, JSONUtil.toJsonPrettyStr(chatmessages));
                }
                default ->
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_SUPPORT_ERR, "不支持的消息类型"), Instant.now().toString()));
            }
        } catch (Exception e) {
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(FORMAT_ERR, "发送消息格式错误请确认后再试"), Instant.now().toString()));
            throw e;
        }
    }

    /**
     * 创建私聊chatMessage对象
     *
     * @param chat       chatMessage对象
     * @param receiverId 消息接收人的Id
     * @param type       消息的类型（离线，未读，已读）
     * @param fromUserId 用户的Id
     * @param status     消息的状态
     * @return chatMessage对象
     */
    private static Chatmessages createChatMessage(ChatMessage chat, Integer receiverId, String type, Integer fromUserId, String status) {
        Chatmessages chatmessages = new Chatmessages();
        chatmessages.setSenderId(fromUserId).setReceiverId(receiverId).setContent(chat.getContent()).setMessageType(type).setTimestamp(LocalDateTime.now()).setStatus(status);
        return chatmessages;
    }

    /**
     * 判断消息接收者是否是用户好友，以及消息接收者是否屏蔽用户
     *
     * @param userId     用户的Id
     * @param receiverId 消息接收者的Id
     * @return 消息接收者是否是用户好友，以及消息接收者是否屏蔽用户的结果
     */
    private static Boolean isBlock(Integer userId, Integer receiverId) {
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        ContactMapper contactMapper = SpringContextUtil.getBean(ContactMapper.class);
        String key = CACHE_BLOCK_KEY + receiverId;
        //从redis获取消息接收者是否屏蔽用户的缓存
        Object blockObject = stringRedisTemplate.opsForHash().get(key, userId.toString());
        if (blockObject == null) {
            //redis中没有信息则查询数据库
            LambdaQueryWrapper<Contact> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Contact::getContactId, userId)
                    .eq(Contact::getUserId, receiverId);
            Contact contact = contactMapper.selectOne(queryWrapper);
            //数据库中如果也没有信息说明根本不是好友像redis添加空值缓存，防止缓存穿透，并返回null
            if (contact == null) {
                stringRedisTemplate.opsForHash().put(key, userId.toString(), "");
                return null;
            }
            Boolean blocked = contact.getBlocked();
            //数据库中有就添加redis缓存，并设置缓存过期时间
            stringRedisTemplate.opsForHash().put(key, userId.toString(), blocked.toString());
            stringRedisTemplate.expire(key, CACHE_BLOCK_TTL, TimeUnit.SECONDS);
            return blocked;
        }
        //查询到是空值代表用户没有添加消息接收者的好友，返回null
        if ("".equals(blockObject.toString())) {
            return null;
        }
        //查询到是正常的block值则直接返回
        String blockStr = blockObject.toString();
        boolean block = Boolean.parseBoolean(blockStr);
        return block;

    }

}
