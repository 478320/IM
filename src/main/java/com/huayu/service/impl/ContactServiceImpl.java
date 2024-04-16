package com.huayu.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.huayu.domain.Chatmessages;
import com.huayu.domain.Contact;
import com.huayu.domain.User;
import com.huayu.dto.LoginUser;
import com.huayu.dto.Result;
import com.huayu.mapper.ContactMapper;
import com.huayu.service.IContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.huayu.utils.RedisConstans.*;

/**
 * contact服务层实现类
 */
@Service
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements IContactService {

    @Autowired
    private ContactMapper contactMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result listContacts() {
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        Integer userId = user.getId();
        String key = CACHE_CONTACT_KEY + userId;
        String connectListJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(connectListJson)) {
            List list = JSONUtil.toList(connectListJson, List.class);
            return Result.ok(list, list.size());
        }
        //如果查询到是空集合，表示未发送消息，反回空集合，判断如果是空集合返回空数据
        if (connectListJson != null) {
            return Result.ok(Collections.emptyList(), 0);
        }
        //redis中没有信息则查询数据库
        LambdaQueryWrapper<Contact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Contact::getUserId, userId);
        List<Contact> contactList = contactMapper.selectList(queryWrapper);
        //数据库没有消息，添加空集合缓存防止缓存穿透
        if (contactList.isEmpty()) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return Result.ok(Collections.emptyList(), 0);
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(contactList), CACHE_CONTACT_TTL, TimeUnit.SECONDS);
        return Result.ok(contactList, contactList.size());
    }

    @Transactional
    @Override
    public Result removeContact(Integer contactId) {
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        Integer userId = user.getId();
        stringRedisTemplate.delete(CACHE_CONTACT_KEY + userId);
        stringRedisTemplate.delete(CACHE_CONTACT_KEY + contactId);
        LambdaQueryWrapper<Contact> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Contact::getUserId, userId);
        queryWrapper.eq(Contact::getContactId, contactId);
        contactMapper.delete(queryWrapper);
        LambdaQueryWrapper<Contact> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Contact::getUserId, contactId);
        queryWrapper1.eq(Contact::getContactId, userId);
        contactMapper.delete(queryWrapper1);
        return Result.ok("删除好友成功");
    }


    @Override
    public Result blockContact(Integer contactId) {
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        Integer userId = user.getId();
        stringRedisTemplate.delete(CACHE_CONTACT_KEY + userId);
        stringRedisTemplate.opsForHash().delete(CACHE_BLOCK_KEY + userId, contactId.toString());
        UpdateWrapper<Contact> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId.toString());
        updateWrapper.eq("contact_id", contactId.toString());
        Contact contact = new Contact();
        contact.setBlocked(true);
        contactMapper.update(contact, updateWrapper);
        return Result.ok("屏蔽成功，你将不会收到该联系人的所有消息");
    }

    @Override
    public Result nBlockContact(Integer contactId) {
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        Integer userId = user.getId();
        stringRedisTemplate.delete(CACHE_CONTACT_KEY + userId);
        stringRedisTemplate.opsForHash().delete(CACHE_BLOCK_KEY + userId, contactId.toString());
        UpdateWrapper<Contact> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId.toString());
        updateWrapper.eq("contact_id", contactId.toString());
        Contact contact = new Contact();
        contact.setBlocked(false);
        contactMapper.update(contact, updateWrapper);
        return Result.ok("取消屏蔽成功，你将重新收到该联系人的所有消息");
    }
}
