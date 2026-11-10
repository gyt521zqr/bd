package com.magao.backend.module.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.course.entity.CourseVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程视频Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface CourseVideoMapper extends BaseMapper<CourseVideo> {

    /** 查询某课程下的全部视频（按排序号升序） */
    @Select("SELECT * FROM course_video WHERE course_id = #{courseId} AND deleted = 0 ORDER BY sort_order ASC, id ASC")
    List<CourseVideo> selectByCourseId(@Param("courseId") Long courseId);
}
