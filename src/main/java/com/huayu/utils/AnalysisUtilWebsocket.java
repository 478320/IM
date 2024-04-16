package com.huayu.utils;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.huayu.dto.LoginUser;
import com.huayu.exception.BusinessException;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.huayu.utils.Code.BUSINESS_ERR;
import static com.huayu.utils.RedisConstans.LOGIN_USER_KEY;

/**
 * 解析token工具类
 */
public class AnalysisUtilWebsocket {

    /**
     * 将token转化为用户Id返回
     *
     * @param token 用户的token信息
     * @return 用户的Id
     */
    public static Integer analysisTokenToUserId(String token) {
        //使用时请确保token不为空
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BUSINESS_ERR, "token非法");
        }
        //从redis中获取用户信息
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String redisKey = LOGIN_USER_KEY + userid;
        String loginUserStr = stringRedisTemplate.opsForValue().get(redisKey);
        LoginUser loginUser = JSONUtil.toBean(loginUserStr, LoginUser.class);
        return loginUser.getUser().getId();
    }

    /**
     * 将token转化为用户名返回
     *
     * @param token 用户的token信息
     * @return 用户的用户名
     */
    public static String analysisTokenToUsername(String token) {
        //使用时请确保token不为空
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(BUSINESS_ERR, "token非法");
        }
        //从redis中获取用户信息
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        String redisKey = LOGIN_USER_KEY + userid;
        String loginUserStr = stringRedisTemplate.opsForValue().get(redisKey);
        LoginUser loginUser = JSONUtil.toBean(loginUserStr, LoginUser.class);
        return loginUser.getUser().getUsername();
    }
}
