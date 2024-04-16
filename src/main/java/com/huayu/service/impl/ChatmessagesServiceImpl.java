package com.huayu.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huayu.domain.Chatmessages;
import com.huayu.domain.GroupMembers;
import com.huayu.domain.User;
import com.huayu.dto.LoginUser;
import com.huayu.dto.Result;
import com.huayu.dto.WebSocketResult;
import com.huayu.mapper.ChatmessagesMapper;
import com.huayu.mapper.GroupMembersMapper;
import com.huayu.service.IChatmessagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayu.utils.AnalysisUtilWebsocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.huayu.utils.RedisConstans.*;

/**
 * chatMessages服务层实现类
 */
@Service
public class ChatmessagesServiceImpl extends ServiceImpl<ChatmessagesMapper, Chatmessages> implements IChatmessagesService {

    @Autowired
    private ChatmessagesMapper chatmessagesMapper;
    @Autowired
    private GroupMembersMapper groupMembersMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Result getChatMessage(Integer receiverId) {
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        Integer sendId = user.getId();
        String key = CACHE_ONE_CHAT_KEY + sendId + ":" + receiverId;
        String chatOneJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(chatOneJson)) {
            List list = JSONUtil.toList(chatOneJson, List.class);
            stringRedisTemplate.opsForValue().set(key, chatOneJson, CACHE_ONE_CHAT_TTL + new Random().nextLong(100), TimeUnit.SECONDS);
            return Result.ok(list, list.size());
        }
        //如果查询到是空集合，表示未发送消息，反回空集合，判断如果是空集合返回空数据
        if (chatOneJson != null) {
            return Result.ok(Collections.emptyList(), 0);
        }
        //redis中没有信息则查询数据库
        LambdaQueryWrapper<Chatmessages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chatmessages::getReceiverId, receiverId)
                .eq(Chatmessages::getMessageType, "单聊")
                .eq(Chatmessages::getSenderId, sendId)
                .eq(Chatmessages::getStatus, 2)
                .select()
                .ge(Chatmessages::getTimestamp, LocalDateTime.now().minusDays(3));
        List<Chatmessages> chatmessages = chatmessagesMapper.selectList(queryWrapper);
        //数据库没有消息，添加空集合缓存防止缓存穿透
        if (chatmessages.isEmpty()) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.ok(Collections.emptyList(), 0);
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(chatmessages), CACHE_ONE_CHAT_TTL + new Random().nextLong(100), TimeUnit.SECONDS);
        return Result.ok(chatmessages, chatmessages.size());
    }

    @Override
    @Transactional
    public Result getGroupMessage(Integer receiverId) {
        //判断用户是否属于该群聊
        LambdaQueryWrapper<GroupMembers> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GroupMembers::getGroupId, receiverId);
        List<GroupMembers> groupMembers = groupMembersMapper.selectList(lambdaQueryWrapper);
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        boolean isBelong = groupMembers.stream().anyMatch(groupMembers1 -> groupMembers1.getUserId().equals(user.getId()));
        //不属于群成员返回失败信息
        if (!isBelong) {
            return Result.fail("您还不是群成员无法观看该群消息");
        }
        Integer sendId = user.getId();
        String key = CACHE_GROUP_CHAT_KEY + receiverId;
        String chatGroupJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(chatGroupJson)) {
            List list = JSONUtil.toList(chatGroupJson, List.class);
            stringRedisTemplate.opsForValue().set(key, chatGroupJson, CACHE_GROUP_CHAT_TTL + new Random().nextLong(100), TimeUnit.SECONDS);
            return Result.ok(list, list.size());
        }
        //如果查询到是空集合，表示未发送消息，反回空集合，判断如果是空集合返回空数据
        if (chatGroupJson != null) {
            return Result.ok(Collections.emptyList(), 0);
        }
        //redis中没有信息则查询数据库
        LambdaQueryWrapper<Chatmessages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chatmessages::getReceiverId, receiverId)
                .eq(Chatmessages::getMessageType, "群聊")
                .eq(Chatmessages::getStatus, 2)
                .ge(Chatmessages::getTimestamp, LocalDateTime.now().minusDays(3));
        List<Chatmessages> chatmessages = chatmessagesMapper.selectList(queryWrapper);
        //数据库没有消息，添加空集合缓存防止缓存穿透
        if (chatmessages.isEmpty()) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.ok(Collections.emptyList(), 0);
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(chatmessages), CACHE_GROUP_CHAT_TTL + new Random().nextLong(100), TimeUnit.SECONDS);
        return Result.ok(chatmessages, chatmessages.size());
    }
}
