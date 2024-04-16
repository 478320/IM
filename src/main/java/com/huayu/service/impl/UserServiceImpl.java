package com.huayu.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayu.domain.User;
import com.huayu.dto.LoginUser;
import com.huayu.dto.Result;
import com.huayu.exception.BusinessException;
import com.huayu.mapper.UserMapper;
import com.huayu.service.IUserService;
import com.huayu.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import static com.huayu.utils.Code.*;
import static com.huayu.utils.RedisConstans.LOGIN_USER_KEY;

/**
 * user服务层实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result login(User user) {
        //AuthenticationManager authenticate进行用户认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        //如果认证没通过，给出对应的提示
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("登录失败");
        }
        //如果认证通过了，使用userid生成一个jwt jwt存入ResponseResult返回
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userid = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userid);
        Map<String, String> map = new HashMap<>();
        map.put("token", jwt);
        //把完整的用户信息存入redis  userid作为key
        String loginUserStr = JSONUtil.toJsonStr(loginUser);
        stringRedisTemplate.opsForValue().set(LOGIN_USER_KEY + userid, loginUserStr, 24, TimeUnit.HOURS);
        return Result.ok("登录成功",map);
    }


    @Override
    public Result register(User user) {
        try {
            creatUserWithPassword(user);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new BusinessException(BUSINESS_ERR, "创建用户失败,该用户名已经被注册,请注意使用合法的字符以及账号密码长度");
        }
        return Result.ok("创建用户成功");
    }

    private User creatUserWithPassword(User user) throws SQLIntegrityConstraintViolationException {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode);
        try {
            save(user);
        } catch (Exception e) {
            throw new SQLIntegrityConstraintViolationException();
        }
        return user;
    }

}
