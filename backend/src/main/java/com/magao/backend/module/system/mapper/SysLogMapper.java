package com.magao.backend.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.system.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}
