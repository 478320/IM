package com.huayu.Session;


import io.netty.channel.Channel;

/**
 * 会话管理接口
 */
public interface Session {

    void setChannel(String channelId,Channel channel,String username);

    Channel getChannel(String channelId);

    String getUsername(String channelId);

    void removeChannel(String channelId);
}
