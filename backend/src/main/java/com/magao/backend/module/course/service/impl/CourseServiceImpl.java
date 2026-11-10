package com.magao.backend.module.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.course.entity.Course;
import com.magao.backend.module.course.entity.CourseVideo;
import com.magao.backend.module.course.mapper.CourseMapper;
import com.magao.backend.module.course.mapper.CourseVideoMapper;
import com.magao.backend.module.course.service.CourseService;
import com.magao.backend.module.course.vo.CourseDetailVO;
import com.magao.backend.module.course.vo.CourseVO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 课程服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseVideoMapper courseVideoMapper;

    @Override
    public Page<CourseVO> pageList(PageDTO pageDTO, String keyword, Long categoryId, Integer status) {
        Page<Course> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getTitle, keyword).or().like(Course::getSubtitle, keyword);
        }
        if (categoryId != null) { wrapper.eq(Course::getCategoryId, categoryId); }
        if (status != null) { wrapper.eq(Course::getStatus, status); }
        wrapper.orderByDesc(Course::getCreateTime);
        Page<Course> coursePage = page(page, wrapper);
        Page<CourseVO> voPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        java.util.List<CourseVO> voList = coursePage.getRecords().stream().map(c -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        Course course = getById(courseId);
        if (course == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        CourseDetailVO vo = new CourseDetailVO();
        BeanUtils.copyProperties(course, vo);
        List<CourseVideo> videos = courseVideoMapper.selectByCourseId(courseId);
        vo.setVideos(videos.stream().map(v -> {
            CourseDetailVO.VideoVO vv = new CourseDetailVO.VideoVO();
            vv.setId(v.getId());
            vv.setTitle(v.getTitle());
            vv.setVideoUrl(v.getVideoUrl());
            vv.setSortOrder(v.getSortOrder());
            return vv;
        }).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public Course createCourse(Course course) {
        course.setStatus(0);
        save(course);
        log.info("课程创建成功：courseId={}, title={}", course.getId(), course.getTitle());
        return course;
    }

    @Override
    public Course updateCourse(Course course) {
        Course existing = getById(course.getId());
        if (existing == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        updateById(course);
        log.info("课程更新成功：courseId={}", course.getId());
        return course;
    }

    @Override
    public void publishCourse(Long courseId) {
        Course course = getById(courseId);
        if (course == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        course.setStatus(1);
        updateById(course);
        log.info("课程发布成功：courseId={}", courseId);
    }

    @Override
    public void offlineCourse(Long courseId) {
        Course course = getById(courseId);
        if (course == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        course.setStatus(2);
        updateById(course);
        log.info("课程下架成功：courseId={}", courseId);
    }

    @Override
    public List<CourseVideo> listVideos(Long courseId) {
        return courseVideoMapper.selectByCourseId(courseId);
    }

    @Override
    public CourseVideo addVideo(CourseVideo video) {
        if (video.getCourseId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (!StringUtils.hasText(video.getTitle())) {
            throw new BusinessException(ErrorCode.PARAM_EMPTY);
        }
        if (video.getSortOrder() == null) {
            video.setSortOrder(0);
        }
        courseVideoMapper.insert(video);
        log.info("课程视频新增成功：videoId={}, courseId={}", video.getId(), video.getCourseId());
        return video;
    }

    @Override
    public CourseVideo updateVideo(CourseVideo video) {
        if (video.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        courseVideoMapper.updateById(video);
        log.info("课程视频更新成功：videoId={}", video.getId());
        return video;
    }

    @Override
    public void deleteVideo(Long videoId) {
        CourseVideo existing = courseVideoMapper.selectById(videoId);
        if (existing == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        courseVideoMapper.deleteById(videoId);
        log.info("课程视频删除成功：videoId={}", videoId);
    }
}
