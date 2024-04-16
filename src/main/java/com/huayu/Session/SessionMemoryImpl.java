package com.huayu.Session;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理实现类
 */
public class SessionMemoryImpl implements Session {

    private final ConcurrentHashMap<String, Channel> channelIdToChannelMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, String> channelIdToUsernameMap = new ConcurrentHashMap<>();

    @Override
    public void removeChannel(String channelId) {
        channelIdToChannelMap.remove(channelId);
        channelIdToUsernameMap.remove(channelId);
    }

    @Override
    public Channel getChannel(String channelId) {
        return channelIdToChannelMap.get(channelId);
    }

    @Override
    public String getUsername(String channelId) {
        return channelIdToUsernameMap.get(channelId);
    }

    @Override
    public void setChannel(String channelId, Channel channel, String username) {
        channelIdToChannelMap.put(channelId, channel);
        channelIdToUsernameMap.put(channelId, username);
    }

    @Override
    public String toString() {
        return channelIdToChannelMap.toString();
    }
}
