package com.huayu.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建群的数据传输对象
 */
@Data
public class GroupCreationRequest {

    private CreateGroupDTO createGroupDTO;

    private List<CreateGroupMembersDTO> createGroupMembersDTOList;
}
