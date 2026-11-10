package com.magao.backend.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.message.entity.MessageTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息模板Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface MessageTemplateMapper extends BaseMapper<MessageTemplate> {
}
