package com.magao.backend.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.user.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录日志Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
}
