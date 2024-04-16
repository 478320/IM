package com.huayu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huayu.domain.User;
import com.huayu.dto.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * user服务类
 */
public interface IUserService extends IService<User> {

    Result login(User user);

    Result register(User user);

}
