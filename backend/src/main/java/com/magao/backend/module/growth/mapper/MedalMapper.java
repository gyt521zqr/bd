package com.magao.backend.module.growth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.growth.entity.Medal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 勋章Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface MedalMapper extends BaseMapper<Medal> {
}
