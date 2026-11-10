package com.magao.backend.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
