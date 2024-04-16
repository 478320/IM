package com.huayu.handler;

import com.huayu.service.ITransactionalNettyService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * WebsocketHandler总处理器
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ITransactionalNettyService transactionalNettyService;

    public WebSocketHandler(ITransactionalNettyService transactionalNettyService) {
        this.transactionalNettyService = transactionalNettyService;
    }

    /**
     * 执行添加了Spring的事务对象
     *
     * @param channelHandlerContext 用户的ctx对象
     * @param textWebSocketFrame    用户信息的载体
     * @throws Exception 异常信息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        transactionalNettyService.executeWithTransaction(channelHandlerContext, textWebSocketFrame);
    }

}
