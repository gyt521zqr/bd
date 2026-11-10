package com.magao.backend.module.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.practice.entity.PracticeDraft;
import org.apache.ibatis.annotations.Mapper;

/**
 * 练习草稿Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface PracticeDraftMapper extends BaseMapper<PracticeDraft> {
}
