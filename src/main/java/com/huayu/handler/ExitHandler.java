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
import java.util.concurrent.TimeUnit;

import static com.huayu.utils.RedisConstans.USER_CHANNEL_KEY;
import static com.huayu.utils.WebSocketErrorConstants.ILLEGAL_TOKEN_ERR;
import static com.huayu.utils.WebSocketErrorConstants.NO_LOGIN_ERR;

/**
 * 断开连接处理器
 */
public class ExitHandler {

    /**
     * handler执行方法
     *
     * @param channelHandlerContext 用户的ctx
     * @param textWebSocketFrame    用户传递信息的载体
     */
    public static void execute(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) {
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        Command command = JSONUtil.toBean(textWebSocketFrame.text(), Command.class);
        String token = command.getToken();
        //判断用户是否登录
        if (token == null) {
            //如果用户未登录则返回对应错误信息
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(NO_LOGIN_ERR, "用户尚未登录无需退出"), Instant.now().toString()));
            return;
        }
        //获取到用户名
        String username = AnalysisUtilWebsocket.analysisTokenToUsername(token);
        if (username == null) {
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.fail("error", new ErrorResult(ILLEGAL_TOKEN_ERR, "token非法"), Instant.now().toString()));
        } else {
            String channelId = channelHandlerContext.channel().id().asLongText();
            //删除map中用户的channel信息防止内存泄漏
            SessionFactory.getSession().removeChannel(channelId);
            //删除用户的在线状态
            stringRedisTemplate.delete(USER_CHANNEL_KEY + username);
            channelHandlerContext.channel().writeAndFlush(WebSocketResult.ok("notification", new SystemNoticeResult("退出登录成功", false), Instant.now().toString()));
            channelHandlerContext.channel().close();
        }
    }
}
