package com.huayu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huayu.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * user数据层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
