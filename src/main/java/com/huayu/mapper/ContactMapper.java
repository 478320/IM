package com.huayu.mapper;

import com.huayu.domain.Contact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友数据层
 */
@Mapper
public interface ContactMapper extends BaseMapper<Contact> {

}
