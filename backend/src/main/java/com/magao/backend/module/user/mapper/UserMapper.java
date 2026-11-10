package com.magao.backend.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * <p>
 * 继承MyBatis-Plus的BaseMapper，提供用户表的CRUD操作
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
