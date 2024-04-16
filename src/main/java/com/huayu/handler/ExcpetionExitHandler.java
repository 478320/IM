package com.huayu.handler;

import com.huayu.Session.Session;
import com.huayu.Session.SessionFactory;
import com.huayu.utils.SpringContextUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.data.redis.core.StringRedisTemplate;
import static com.huayu.utils.RedisConstans.USER_CHANNEL_KEY;


/**
 * 意外断开连接处理器
 */
@ChannelHandler.Sharable
public class ExcpetionExitHandler extends ChannelInboundHandlerAdapter {

    /**
     * 处理用户意外断开连接方法
     *
     * @param ctx 用户的ctx对象
     * @throws Exception 异常信息
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String channelId = ctx.channel().id().asLongText();
        Session session = SessionFactory.getSession();
        //获取到用户名
        String username = session.getUsername(channelId);
        //删除用户的在线状态
        stringRedisTemplate.delete(USER_CHANNEL_KEY + username);
        //删除map中用户的channel信息防止内存泄漏
        session.removeChannel(channelId);
    }

    /**
     * 处理因为异常导致用户断开连接的情况
     *
     * @param ctx 用户的ctx对象
     * @param cause 错误原因
     * @throws Exception 异常信息
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String channelId = ctx.channel().id().asLongText();
        Session session = SessionFactory.getSession();
        //获取到用户名
        String username = session.getUsername(channelId);
        //删除用户的在线状态
        stringRedisTemplate.delete(USER_CHANNEL_KEY + username);
        //删除map中用户的channel信息防止内存泄漏
        session.removeChannel(channelId);
    }
}
