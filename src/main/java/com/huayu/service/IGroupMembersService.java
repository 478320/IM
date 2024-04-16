package com.huayu.service;

import com.huayu.domain.GroupMembers;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * GroupMembers服务层
 */
public interface IGroupMembersService extends IService<GroupMembers> {
    public Boolean saveEntities(List<GroupMembers> list);
}
