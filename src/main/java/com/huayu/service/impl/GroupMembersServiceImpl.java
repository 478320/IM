package com.huayu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huayu.domain.GroupMembers;
import com.huayu.mapper.GroupMembersMapper;
import com.huayu.service.IGroupMembersService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  GroupMembers服务层实现类
 */
@Service
public class GroupMembersServiceImpl extends ServiceImpl<GroupMembersMapper, GroupMembers> implements IGroupMembersService {
    @Override
    public Boolean saveEntities(List<GroupMembers> list) {
        return this.saveBatch(list);
    }
}
