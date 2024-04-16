package com.huayu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.huayu.domain.GroupMembers;
import com.huayu.domain.Groups;
import com.huayu.domain.User;
import com.huayu.dto.*;
import com.huayu.mapper.GroupMembersMapper;
import com.huayu.mapper.GroupsMapper;
import com.huayu.service.IGroupMembersService;
import com.huayu.service.IGroupsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Group服务层实现类
 */
@Service
public class GroupsServiceImpl extends ServiceImpl<GroupsMapper, Groups> implements IGroupsService {

    @Autowired
    private GroupsMapper groupsMapper;

    @Autowired
    private IGroupMembersService groupMembersService;

    @Override
    @Transactional
    public Result createGroup(GroupCreationRequest groupCreationRequest) {
        //TODO 判断用户是否是联系人，是才能拉取成功
        CreateGroupDTO createGroupDTO = groupCreationRequest.getCreateGroupDTO();
        List<CreateGroupMembersDTO> list = groupCreationRequest.getCreateGroupMembersDTOList();
        Groups group = BeanUtil.copyProperties(createGroupDTO, Groups.class);
        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principal.getUser();
        group.setCreatedAt(LocalDateTime.now());
        group.setCreatorId(user.getId());
        groupsMapper.insert(group);
        int groupId = group.getGroupId();
        List<GroupMembers> collect = list.stream()
                .map(createGroupMembersDTO -> BeanUtil.copyProperties(createGroupMembersDTO, GroupMembers.class))
                .collect(Collectors.toList());
        collect.stream().forEach(groupMembers -> {
            groupMembers.setGroupId(groupId);
            groupMembers.setJoinedAt(LocalDateTime.now());
        });
        groupMembersService.saveEntities(collect);
        return Result.ok("创建群成功", null);
    }
}
