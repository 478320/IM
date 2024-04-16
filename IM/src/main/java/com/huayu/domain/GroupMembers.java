package com.huayu.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author huayu
 * @since 2024-03-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_group_members")
public class GroupMembers implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "member_id", type = IdType.AUTO)
    private Integer memberId;

    @TableField("user_id")
    private Integer userId;

    @TableField("group_id")
    private Integer groupId;

    @TableField("joined_at")
    private LocalDateTime joinedAt;

    @TableField("role")
    private String role;

    @TableField("last_read_at")
    private LocalDateTime lastReadAt;


}
