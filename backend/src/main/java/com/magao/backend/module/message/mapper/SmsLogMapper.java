package com.magao.backend.module.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.message.entity.SmsLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信日志Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLog> {
}
