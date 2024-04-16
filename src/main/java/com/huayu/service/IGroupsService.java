package com.huayu.service;

import com.huayu.domain.GroupMembers;
import com.huayu.domain.Groups;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huayu.dto.CreateGroupDTO;
import com.huayu.dto.CreateGroupMembersDTO;
import com.huayu.dto.GroupCreationRequest;
import com.huayu.dto.Result;

import java.util.List;

/**
 * Groups服务层
 */
public interface IGroupsService extends IService<Groups> {

    Result createGroup(GroupCreationRequest groupCreationRequest);
}
