package com.huayu.handler;

import cn.hutool.json.JSONUtil;
import com.huayu.Session.SessionFactory;
import com.huayu.domain.ErrorResult;
import com.huayu.domain.SystemNoticeResult;
import com.huayu.dto.WebSocketResult;
import com.huayu.utils.AnalysisUtilWebsocket;
import com.huayu.utils.SpringContextUtil;
import com.huayu.webSocket.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.huayu.utils.RedisConstans.*;
import static com.huayu.utils.WebSocketErrorConstants.ILLEGAL_TOKEN_ERR;
import static com.huayu.utils.WebSocketErrorConstants.NO_LOGIN_ERR;


/**
 * 用来处理连接申请的处理器
 */
public class ConnectionHandler {

    /**
     * handler执行方法
     *
     * @param channelHandlerContext 用户的ctx
     * @param textWebSocketFrame    用户传递信息的载体
     */
    public static void execute(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) {
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        Command command = JSONUtil.toBean(textWebSocketFrame.text(), Command.class);
        //从消息中获取用户的token信息
        String token = command.getToken();
        //判断token是否为空
        if (token == null) {
            //如果token为空则通知用户对应的信息
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_LOGIN_ERR, "请登录"), Instant.now().toString()));
            return;
        }
        //利用token分析出用户的用户名和userId
        String username = AnalysisUtilWebsocket.analysisTokenToUsername(token);
        Integer userId = AnalysisUtilWebsocket.analysisTokenToUserId(token);
        //判断用户名是否为空
        if (username == null) {
            //用户名为空则告诉用户对应的错误信息
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(ILLEGAL_TOKEN_ERR, "token非法"), Instant.now().toString()));
        } else {
            //获取到用户channel的Id
            String channelId = channelHandlerContext.channel().id().asLongText();
            //向Session类的map集合中以channelId为key放入channel，以及向Session类的map集合中以channelId为key放入username信息username将用于处理异常断开情况
            SessionFactory.getSession().setChannel(channelId, channelHandlerContext.channel(), username);
            //向redis中存入用户的登录信息，以及对应的channelId
            stringRedisTemplate.opsForValue().set(USER_CHANNEL_KEY + username, channelId, 24, TimeUnit.HOURS);
            //通知用户连接成功
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult("连接成功", false), Instant.now().toString()));
            //判断用户的好友申请列表是否有好友申请
            if (stringRedisTemplate.opsForHash().get(HAVE_FRIEND_REQUEST_KEY, userId.toString()) != null) {
                //如果有好友申请则通知用户处理
                channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult("您有待处理的好友请求", true), Instant.now().toString()));
                //删除对应的好友申请缓存
                stringRedisTemplate.opsForHash().delete(HAVE_FRIEND_REQUEST_KEY, userId.toString());
            }
            //从redis中获取用户发送的好友申请成功列表
            Set<String> members = stringRedisTemplate.opsForSet().members(CONNECT_SUCCESS_KEY + userId);
            //判断成功的结果是否为空
            if (!members.isEmpty()) {
                //如果不为空则通知用户哪些好友通过了用户的好友申请
                for (String member : members) {
                    channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult(member + "同意了您的好友请求", false), Instant.now().toString()));
                }
                //删除成功列表的缓存
                stringRedisTemplate.delete(CONNECT_SUCCESS_KEY + userId);
            }
            //从redis中获取用户发送的好友申请失败列表
            Set<String> members2 = stringRedisTemplate.opsForSet().members(CONNECT_FAIL_KEY + userId);
            //判断失败的结果是否为空
            if (!members2.isEmpty()) {
                //如果不为空则通知用户哪些好友拒绝了用户的好友申请
                for (String member : members2) {
                    channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult(member + "拒绝了您的好友请求", false), Instant.now().toString()));
                }
                //删除失败列表的缓存
                stringRedisTemplate.delete(CONNECT_FAIL_KEY + userId);
            }
        }
    }
}
