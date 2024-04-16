package com.huayu.controller;


import com.huayu.dto.GroupCreationRequest;
import com.huayu.dto.Result;
import com.huayu.service.IGroupsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 群表现层对象
 */
@RestController
@RequestMapping("/groups")
public class GroupsController {

    @Autowired
    private IGroupsService groupsService;

    /**
     * 创建群聊
     *
     * @param groupCreationRequest 关于群聊的所有信息，包含群名描述，群成员等
     * @return 创建是否成功的结果
     */
    @PostMapping("/save")
    public Result createGroup(@RequestBody GroupCreationRequest groupCreationRequest){
        return groupsService.createGroup(groupCreationRequest);
    }
}
