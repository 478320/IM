package com.huayu.mapper;

import com.huayu.domain.Chatmessages;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息数据层
 */
@Mapper
public interface ChatmessagesMapper extends BaseMapper<Chatmessages> {

}
