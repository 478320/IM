package com.huayu.service;

import com.huayu.dto.WebSocketResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * TransactionalNetty服务层
 */
public interface ITransactionalNettyService {

    void executeWithTransaction(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame);
}
