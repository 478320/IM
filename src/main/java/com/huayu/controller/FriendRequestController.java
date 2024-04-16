package com.huayu.controller;


import com.huayu.dto.Result;
import com.huayu.service.IFriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 好友申请表现层
 */
@RestController
@RequestMapping("/friend-request")
public class FriendRequestController {

    @Autowired
    private IFriendRequestService friendRequestService;

    /**
     * 获取所有申请我为好友的信息
     *
     * @return 所有添加我好友的信息列表
     */
    @GetMapping("/get/me")
    public Result listConnectMe() {
        return friendRequestService.listConnectMe();
    }

}
