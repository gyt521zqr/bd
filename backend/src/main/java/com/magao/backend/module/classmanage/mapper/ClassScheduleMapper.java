package com.magao.backend.module.classmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.classmanage.entity.ClassSchedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级排课Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface ClassScheduleMapper extends BaseMapper<ClassSchedule> {
}
