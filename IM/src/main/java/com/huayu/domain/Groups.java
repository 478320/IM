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
@TableName("tb_groups")
public class Groups implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "group_id", type = IdType.AUTO)
    private Integer groupId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("creator_id")
    private Integer creatorId;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("status")
    private String status;


}
