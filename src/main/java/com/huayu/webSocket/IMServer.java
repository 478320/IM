package com.huayu.webSocket;

import com.huayu.dto.WebSocketResult;
import com.huayu.handler.ExcpetionExitHandler;
import com.huayu.handler.WebSocketHandler;
import com.huayu.utils.SpringContextUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Netty服务器
 */
public class IMServer {

    /**
     * netty服务开启方法
     */
    public static void start() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        ExcpetionExitHandler EXCEPTION_EXIT_HANDLER = new ExcpetionExitHandler();
        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //用于判断空闲时间是否超过了某个长度
                            pipeline.addLast(new IdleStateHandler(3600,0,0))
                                    //自定义入站出站心跳机制处理器
                                    .addLast(new ChannelDuplexHandler(){
                                        //触发特殊事件
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                                                ctx.channel().writeAndFlush(WebSocketResult.fail("error","用户网络连接异常", Instant.now().toString()));
                                            }
                                            super.userEventTriggered(ctx, evt);
                                        }
                                    })
                                    .addLast(new HttpServerCodec())
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpObjectAggregator(1024 * 64))
                                    .addLast(new WebSocketServerProtocolHandler("/"))
                                    .addLast(SpringContextUtil.getBean(WebSocketHandler.class))
                                    //添加异常断卡链接处理器
                                    .addLast(EXCEPTION_EXIT_HANDLER);
                        }
                    });

            Channel channel = bootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {

        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
