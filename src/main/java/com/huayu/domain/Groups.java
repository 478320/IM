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
 * 群实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_groups")
public class Groups implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "group_id", type = IdType.AUTO)
    private Integer groupId;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private Integer creatorId;

    private String avatarUrl;

    private String status;


}
