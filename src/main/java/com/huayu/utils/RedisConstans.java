package com.huayu.utils;

/**
 * Redis数据库使用的常量
 */
public class RedisConstans {

    public static final String LOGIN_USER_KEY = "login:";

    public static final Long LOGIN_USER_TTL = 34200L;

    public static final String USER_CHANNEL_KEY = "user:channel:";

    public static final Long CACHE_NULL_TTL = 2L;

    public static final String CACHE_GROUP_CHAT_KEY = "cache:groupChat:";

    public static final String CACHE_ONE_CHAT_KEY = "cache:oneChat:";

    public static final String HAVE_FRIEND_REQUEST_KEY = "have:friendRequest:";

    public static final String CONNECT_FAIL_KEY = "connect:fail:";

    public static final String CONNECT_SUCCESS_KEY = "connect:success:";

    public static final String CACHE_CONTACT_KEY = "cache:contact:";

    public static final String CACHE_FRIEND_KEY = "cache:friend:";

    public static final String CACHE_BLOCK_KEY = "cache:block:";


    public static final Long CACHE_CONTACT_TTL = 1800L;

    public static final Long CACHE_ONE_CHAT_TTL = 1800L;

    public static final Long CACHE_GROUP_CHAT_TTL = 1800L;

    public static final Long CACHE_BLOCK_TTL = 3600L;


}
