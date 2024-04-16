package com.huayu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群成员实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_group_members")
public class GroupMembers implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "member_id", type = IdType.AUTO)
    private Integer memberId;

    private Integer userId;

    private Integer groupId;

    private LocalDateTime joinedAt;

    private String role;

    private LocalDateTime lastReadAt;


}
