package com.magao.backend.module.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
