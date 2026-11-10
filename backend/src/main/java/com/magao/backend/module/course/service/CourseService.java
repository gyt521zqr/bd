package com.magao.backend.module.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.course.entity.Course;
import com.magao.backend.module.course.entity.CourseVideo;
import com.magao.backend.module.course.vo.CourseDetailVO;
import com.magao.backend.module.course.vo.CourseVO;

import java.util.List;

/**
 * 课程服务接口
 * <p>
 * 定义课程模块的业务方法
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface CourseService extends IService<Course> {

    /** 分页查询课程列表 */
    Page<CourseVO> pageList(PageDTO pageDTO, String keyword, Long categoryId, Integer status);

    /** 获取课程详情 */
    CourseDetailVO getCourseDetail(Long courseId);

    /** 创建课程（管理员） */
    Course createCourse(Course course);

    /** 更新课程（管理员） */
    Course updateCourse(Course course);

    /** 发布课程（管理员） */
    void publishCourse(Long courseId);

    /** 下架课程（管理员） */
    void offlineCourse(Long courseId);

    /** 查询课程视频列表（按排序号升序） */
    List<CourseVideo> listVideos(Long courseId);

    /** 新增课程视频 */
    CourseVideo addVideo(CourseVideo video);

    /** 更新课程视频 */
    CourseVideo updateVideo(CourseVideo video);

    /** 删除课程视频 */
    void deleteVideo(Long videoId);
}
