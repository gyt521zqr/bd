package com.magao.backend.module.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.cms.entity.Category;
import com.magao.backend.module.cms.service.CategoryService;
import com.magao.backend.module.course.entity.Course;
import com.magao.backend.module.course.entity.CoursePurchase;
import com.magao.backend.module.course.entity.CourseVideo;
import com.magao.backend.module.course.entity.VideoProgress;
import com.magao.backend.module.course.mapper.CourseMapper;
import com.magao.backend.module.course.mapper.CoursePurchaseMapper;
import com.magao.backend.module.course.mapper.CourseVideoMapper;
import com.magao.backend.module.course.mapper.VideoProgressMapper;
import com.magao.backend.module.course.service.CourseService;
import com.magao.backend.module.course.vo.CourseDetailVO;
import com.magao.backend.module.course.vo.CourseVO;
import com.magao.backend.module.payment.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 课程管理控制器
 * <p>
 * 提供课程相关的API接口：列表、详情、创建、更新、发布
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "课程管理", description = "课程查询、创建、管理等接口")
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController extends BaseController {

    private final CourseService courseService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final CourseVideoMapper courseVideoMapper;
    private final VideoProgressMapper videoProgressMapper;
    private final CourseMapper courseMapper;
    private final CoursePurchaseMapper coursePurchaseMapper;

    /**
     * P0-08：付费课程播放/进度校验。免费课程（price=0 或 null）放行；
     * 付费课程必须存在有效的购买记录（未过期）才能访问，杜绝白嫖。
     */
    private void assertCourseAccessible(Long userId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "课程不存在");
        }
        if (course.getPrice() == null || course.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return; // 免费课程
        }
        LambdaQueryWrapper<CoursePurchase> qw = new LambdaQueryWrapper<>();
        qw.eq(CoursePurchase::getUserId, userId)
          .eq(CoursePurchase::getCourseId, courseId);
        CoursePurchase purchase = coursePurchaseMapper.selectOne(qw);
        if (purchase == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "请先购买该课程后再学习");
        }
        if (purchase.getExpireTime() != null && LocalDateTime.now().isAfter(purchase.getExpireTime())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "课程学习权限已过期");
        }
    }

    @Operation(summary = "课程列表")
    @GetMapping("/list")
    public Result<Page<CourseVO>> list(PageDTO pageDTO, @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Long categoryId, @RequestParam(required = false) Integer status) {
        return Result.success(courseService.pageList(pageDTO, keyword, categoryId, status));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/{id}")
    public Result<CourseDetailVO> detail(@PathVariable Long id) {
        return Result.success(courseService.getCourseDetail(id));
    }

    @Operation(summary = "创建课程")
    @PostMapping("/admin/create")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "课程管理", type = "新增", description = "创建课程")
    public Result<Course> create(@Valid @RequestBody Course course) {
        return Result.success(courseService.createCourse(course));
    }

    @Operation(summary = "更新课程")
    @PutMapping("/admin/update")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "课程管理", type = "修改", description = "更新课程")
    public Result<Course> update(@Valid @RequestBody Course course) {
        return Result.success(courseService.updateCourse(course));
    }

    @Operation(summary = "发布课程")
    @PutMapping("/admin/publish/{id}")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "课程管理", type = "修改", description = "发布课程")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publishCourse(id);
        return Result.success();
    }

    @Operation(summary = "课程视频列表")
    @GetMapping("/{id}/videos")
    public Result<List<CourseVideo>> videos(@PathVariable Long id) {
        return Result.success(courseService.listVideos(id));
    }

    @Operation(summary = "新增课程视频")
    @PostMapping("/video")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "课程管理", type = "新增", description = "新增课程视频")
    public Result<CourseVideo> addVideo(@RequestBody CourseVideo video) {
        return Result.success(courseService.addVideo(video));
    }

    @Operation(summary = "更新课程视频")
    @PutMapping("/video")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "课程管理", type = "修改", description = "更新课程视频")
    public Result<CourseVideo> updateVideo(@RequestBody CourseVideo video) {
        return Result.success(courseService.updateVideo(video));
    }

    @Operation(summary = "删除课程视频")
    @DeleteMapping("/video/{id}")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "课程管理", type = "删除", description = "删除课程视频")
    public Result<Void> deleteVideo(@PathVariable Long id) {
        courseService.deleteVideo(id);
        return Result.success();
    }

    // ===================== Phase 1 补齐（对齐前端 src/api/course.ts） =====================

    @Operation(summary = "购买课程")
    @PostMapping("/purchase")
    public Result<Map<String, Object>> purchase(@RequestBody PurchaseReq req) {
        if (req.getCourseId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "课程ID不能为空");
        }
        // 复用支付模块创建前端订单（商品类型 course），返回订单ID供前端拉起支付
        var orderVO = orderService.createOrderFrontend(getCurrentUserId(), "course", req.getCourseId(), req.getCouponId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderVO.getId());
        data.put("orderNo", orderVO.getOrderNo());
        return Result.success(data);
    }

    @Operation(summary = "记录学习进度")
    @PostMapping("/progress")
    public Result<Void> progress(@RequestBody ProgressReq req) {
        if (req.getVideoId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "视频ID不能为空");
        }
        Long userId = getCurrentUserId();
        // P0-08：记录付费课程进度前先校验购买
        CourseVideo video = courseVideoMapper.selectById(req.getVideoId());
        if (video != null && video.getCourseId() != null) {
            assertCourseAccessible(userId, video.getCourseId());
        }
        LambdaQueryWrapper<VideoProgress> qw = new LambdaQueryWrapper<>();
        qw.eq(VideoProgress::getUserId, userId).eq(VideoProgress::getVideoId, req.getVideoId());
        VideoProgress existing = videoProgressMapper.selectOne(qw);
        int progress = req.getProgress() == null ? 0 : req.getProgress();
        if (existing == null) {
            VideoProgress vp = new VideoProgress();
            vp.setUserId(userId);
            vp.setVideoId(req.getVideoId());
            vp.setWatchedDuration(progress);
            vp.setIsCompleted(progress >= 95 ? 1 : 0);
            vp.setLastWatchTime(LocalDateTime.now());
            videoProgressMapper.insert(vp);
        } else {
            existing.setWatchedDuration(progress);
            existing.setIsCompleted(progress >= 95 ? 1 : 0);
            existing.setLastWatchTime(LocalDateTime.now());
            videoProgressMapper.updateById(existing);
        }
        return Result.success();
    }

    @Operation(summary = "视频播放地址（加密）")
    @GetMapping("/video/{id}/play")
    public Result<Map<String, Object>> play(@PathVariable Long id) {
        CourseVideo video = courseVideoMapper.selectById(id);
        if (video == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        // P0-08：付费课程必须先购买才能拿播放地址
        Long userId = getCurrentUserId();
        if (video.getCourseId() != null) {
            assertCourseAccessible(userId, video.getCourseId());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("url", video.getVideoUrl());
        data.put("token", "");
        return Result.success(data);
    }

    @Operation(summary = "课程分类列表（公开）")
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        List<Category> list = categoryService.listByType("course");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category c : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("icon", "");
            result.add(m);
        }
        return Result.success(result);
    }

    /** 购买课程请求体 */
    public static class PurchaseReq {
        private Long courseId;
        private Long couponId;
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Long getCouponId() { return couponId; }
        public void setCouponId(Long couponId) { this.couponId = couponId; }
    }

    /** 学习进度请求体 */
    public static class ProgressReq {
        private Long videoId;
        private Integer progress;
        public Long getVideoId() { return videoId; }
        public void setVideoId(Long videoId) { this.videoId = videoId; }
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
    }
}
