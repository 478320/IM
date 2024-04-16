package com.huayu.controller;


import com.huayu.domain.User;
import com.huayu.dto.Result;
import com.huayu.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * user的表现层对象
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 登录
     *
     * @param user 登录的用户
     * @return 用户登录凭证
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        //登录
        return userService.login(user);
    }

    /**
     * 注册
     *
     * @param user 注册的用户
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

}
