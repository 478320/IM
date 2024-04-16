package com.huayu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huayu.domain.FriendRequest;
import com.huayu.domain.User;
import com.huayu.dto.LoginUser;
import com.huayu.dto.Result;
import com.huayu.mapper.FriendRequestMapper;
import com.huayu.service.IFriendRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FriendRequest服务层实现类
 */
@Service
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest> implements IFriendRequestService {

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Override
    public Result listConnectMe() {
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        Integer userId = user.getId();
        LambdaQueryWrapper<FriendRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequest::getAddresseeId, userId);
        List<FriendRequest> friendRequests = friendRequestMapper.selectList(queryWrapper);
        return Result.ok(friendRequests, friendRequests.size());
    }
}
