package com.huayu.service.impl;

import cn.hutool.json.JSONUtil;
import com.huayu.dto.Result;
import com.huayu.dto.WebSocketResult;
import com.huayu.handler.ChatHandler;
import com.huayu.handler.ConnectionHandler;
import com.huayu.handler.ContactHandler;
import com.huayu.handler.ExitHandler;
import com.huayu.service.ITransactionalNettyService;
import com.huayu.webSocket.Command;
import com.huayu.webSocket.CommandType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Websocket事务服务层实现类
 */
@Component
public class TransactionalNettyServiceImpl implements ITransactionalNettyService {

    @Override
    @Transactional
    public void executeWithTransaction(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) {
        try {
            Command command = JSONUtil.toBean(textWebSocketFrame.text(), Command.class);
            //判断消息的code走不同的handler
            switch (CommandType.match(command.getCode())) {
                case CONNECTION -> ConnectionHandler.execute(channelHandlerContext, textWebSocketFrame);
                case CHAT -> ChatHandler.execute(channelHandlerContext, textWebSocketFrame);
                case CONTACT -> ContactHandler.execute(channelHandlerContext, textWebSocketFrame);
                case EXIT -> ExitHandler.execute(channelHandlerContext, textWebSocketFrame);
                default -> channelHandlerContext.channel().writeAndFlush(Result.fail("不支持的CODE"));
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
