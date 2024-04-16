package com.huayu.handler;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huayu.Session.SessionFactory;
import com.huayu.command.ContactMessage;
import com.huayu.domain.*;
import com.huayu.dto.WebSocketResult;
import com.huayu.mapper.ContactMapper;
import com.huayu.mapper.FriendRequestMapper;
import com.huayu.mapper.UserMapper;
import com.huayu.utils.AnalysisUtilWebsocket;
import com.huayu.utils.SpringContextUtil;
import com.huayu.webSocket.ContactMessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.StringUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;

import static com.huayu.utils.RedisConstans.*;
import static com.huayu.utils.WebSocketErrorConstants.*;

/**
 * 好友申请处理器
 */
public class ContactHandler {

    /**
     * handler执行方法
     *
     * @param channelHandlerContext 用户的ctx
     * @param textWebSocketFrame    用户传递信息的载体
     */
    public static void execute(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) {
        try {
            //获取到对应的Message对象
            ContactMessage chat = JSONUtil.toBean(textWebSocketFrame.text(), ContactMessage.class);
            //获取到用户选择的用户的用户名
            String target = chat.getTarget();
            StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
            FriendRequestMapper friendRequestMapper = SpringContextUtil.getBean(FriendRequestMapper.class);
            ContactMapper contactMapper = SpringContextUtil.getBean(ContactMapper.class);
            UserMapper userMapper = SpringContextUtil.getBean(UserMapper.class);
            // 判断消息的类型，如果是1则走发送好友申请
            switch (ContactMessageType.match(chat.getType())) {
                case SEND -> {
                    //判断用户是否选择了发送人
                    if (StringUtil.isNullOrEmpty(target)) {
                        //为选择发送人给出对应的错误提示
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_POINT_ERR, "添加好友请求发送失败，发送请求前前请指定发送对象"), Instant.now().toString()));
                        return;
                    }
                    // 获取到消息接收者的channelId
                    String targetChannelId = stringRedisTemplate.opsForValue().get(USER_CHANNEL_KEY + target);
                    Integer fromUserId = AnalysisUtilWebsocket.analysisTokenToUserId(chat.getToken());
                    //根据用户名查询到用户的ID这是最早期设计上的失误，将会改善
                    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(User::getUsername, chat.getTarget());
                    Integer receiverId = userMapper.selectOne(queryWrapper).getId();
                    //判断消息接收者是否在线
                    if (targetChannelId == null) {
                        //查询好友申请
                        LambdaQueryWrapper<FriendRequest> queryWrapper1 = new LambdaQueryWrapper<>();
                        queryWrapper1.eq(FriendRequest::getRequesterId, fromUserId);
                        queryWrapper1.eq(FriendRequest::getAddresseeId, receiverId);
                        FriendRequest friendRequest1 = friendRequestMapper.selectOne(queryWrapper1);
                        //判断是否发送过好友申请
                        if (friendRequest1 != null) {
                            //发送过好友申请则给出对应的错误提示
                            channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("error", new ErrorResult(REPEAT_FRIEND_APPLICATION_ERR, "好友申请以发送，请勿重复发送好友申请"), Instant.now().toString()));
                            return;
                        }
                        //封装好友申请对象
                        FriendRequest friendRequest = createFriendRequest(fromUserId, receiverId);
                        friendRequestMapper.insert(friendRequest);
                        //向redis中放入一个键，用户上线后判断这个键中是否存在，存在则系统发送消息有好友请求待处理
                        stringRedisTemplate.opsForHash().put(HAVE_FRIEND_REQUEST_KEY, receiverId.toString(), "1");
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult("发送成功", false), Instant.now().toString()));
                    } else {
                        //查询好友申请
                        LambdaQueryWrapper<FriendRequest> queryWrapper1 = new LambdaQueryWrapper<>();
                        queryWrapper1.eq(FriendRequest::getRequesterId, fromUserId);
                        queryWrapper1.eq(FriendRequest::getAddresseeId, receiverId);
                        FriendRequest friendRequest1 = friendRequestMapper.selectOne(queryWrapper1);
                        //判断是否发送过好友申请
                        if (friendRequest1 != null) {
                            //发送过好友申请则给出对应的错误提示
                            channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("error", new ErrorResult(REPEAT_FRIEND_APPLICATION_ERR, "好友申请以发送，请勿重复发送好友申请"), Instant.now().toString()));
                            return;
                        }
                        //封装数据库消息信息
                        FriendRequest friendRequest = createFriendRequest(fromUserId, receiverId);
                        friendRequestMapper.insert(friendRequest);
                        //发送消息给用户
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult("发送成功", false), Instant.now().toString()));
                        Channel channel = SessionFactory.getSession().getChannel(targetChannelId);
                        //发送消息及时通知用户
                        channel.writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult("有一条好友请求待处理", true), Instant.now().toString()));
                    }
                }
                // 判断消息的类型，如果是2则走确认好友申请
                case CONFIRM -> {
                    //判断用户是否指定了要确认的消息的对象
                    if (StringUtil.isNullOrEmpty(target)) {
                        //如果用户未指定要处理的好友请求则向用户发送错误信息
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("确认好友请求", "确认失败，请指定要确认申请的对象", Instant.now().toString()));
                        return;
                    }
                    String targetChannelId = stringRedisTemplate.opsForValue().get(USER_CHANNEL_KEY + target);
                    Integer userId = AnalysisUtilWebsocket.analysisTokenToUserId(chat.getToken());
                    String userName = AnalysisUtilWebsocket.analysisTokenToUsername(chat.getToken());
                    //根据用户名查询到用户的ID这是最早期设计上的失误，将改善
                    LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(User::getUsername, chat.getTarget());
                    Integer fromUserId = userMapper.selectOne(queryWrapper1).getId();
                    //查询是否真的收到了好友申请
                    LambdaQueryWrapper<FriendRequest> queryWrapper2 = new LambdaQueryWrapper<>();
                    queryWrapper2.eq(FriendRequest::getRequesterId, fromUserId);
                    queryWrapper2.eq(FriendRequest::getAddresseeId, userId);
                    queryWrapper2.eq(FriendRequest::getStatus, "pending");
                    FriendRequest friendRequest = friendRequestMapper.selectOne(queryWrapper2);
                    if (friendRequest == null) {
                        //实际未收到该好友的好友申请，则通知用户对应的信息
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_POINT_ERR, "确认失败，对方尚未向你发送好友请求"), Instant.now().toString()));
                        return;
                    }
                    //判断好友申请发送人是否在线
                    if (targetChannelId == null) {
                        //判断用户是否同意好友申请
                        if (chat.getAgreement()) {
                            friendRequest.setStatus("accepted");
                            friendRequestMapper.updateById(friendRequest);
                            //先删redis好友缓存后，向数据库添加新的好友
                            stringRedisTemplate.delete(CACHE_CONTACT_KEY + userId);
                            stringRedisTemplate.delete(CACHE_CONTACT_KEY + fromUserId);
                            contactMapper.insert(createContact(userId, fromUserId));
                            contactMapper.insert(createContact(fromUserId, userId));
                            //向redis里存入一个提示，用户上线后获取对方同意添加好友的消息
                            stringRedisTemplate.opsForSet().add(CONNECT_SUCCESS_KEY + fromUserId, userName);
                        } else {
                            friendRequest.setStatus("rejected");
                            friendRequestMapper.updateById(friendRequest);
                            //向redis里存入一个提示，用户上线后获取对方拒绝添加好友的消息
                            stringRedisTemplate.opsForSet().add(CONNECT_FAIL_KEY + fromUserId, userName);
                        }
                    } else {
                        Channel channel = SessionFactory.getSession().getChannel(targetChannelId);
                        //判断用户是否同意好友申请
                        if (chat.getAgreement()) {
                            friendRequest.setStatus("accepted");
                            friendRequestMapper.updateById(friendRequest);
                            //先删redis好友缓存后，向数据库添加新的好友
                            stringRedisTemplate.delete(CACHE_CONTACT_KEY + userId);
                            stringRedisTemplate.delete(CACHE_CONTACT_KEY + fromUserId);
                            contactMapper.insert(createContact(userId, fromUserId));
                            contactMapper.insert(createContact(fromUserId, userId));
                            channel.writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult(userName + "同意了你的好友申请", false), Instant.now().toString()));
                        } else {
                            friendRequest.setStatus("rejected");
                            friendRequestMapper.updateById(friendRequest);
                            //向redis里存入一个提示，用户上线后获取对方拒绝添加好友的消息
                            stringRedisTemplate.opsForSet().add(CONNECT_FAIL_KEY + fromUserId, userName);
                            channel.writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult(userName + "拒绝了你的好友申请", false), Instant.now().toString()));
                        }
                    }
                }
                default ->
                        channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_SUPPORT_ERR, "不支持的好友处理业务"), Instant.now().toString()));
            }
        } catch (Exception e) {
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(UNKNOWN_ERR, "好友申请中出现未知的错误"), Instant.now().toString()));
            throw e;
        }
    }

    /**
     * 创建好友申请对象
     *
     * @param requesterId 用户的id
     * @param addresseeId 好友申请接受者的Id
     * @return 好友申请对象
     */
    private static FriendRequest createFriendRequest(Integer requesterId, Integer addresseeId) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequesterId(requesterId);
        friendRequest.setAddresseeId(addresseeId);
        friendRequest.setStatus("pending");
        return friendRequest;
    }

    /**
     * 创建好友对象
     *
     * @param userId 用户id
     * @param contactId 好友id
     * @return 好友对象
     */
    private static Contact createContact(Integer userId, Integer contactId) {
        Contact contact = new Contact();
        contact.setUserId(userId);
        contact.setContactId(contactId);
        return contact;
    }
}
