package com.magao.backend.module.classmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.classmanage.entity.ClassStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级学生Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface ClassStudentMapper extends BaseMapper<ClassStudent> {
}
