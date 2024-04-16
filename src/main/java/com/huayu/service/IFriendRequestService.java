package com.huayu.service;

import com.huayu.domain.FriendRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huayu.dto.Result;

/**
 * FriendRequest服务层
 */
public interface IFriendRequestService extends IService<FriendRequest> {

    Result listConnectMe();
}
