package com.magao.backend.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.cms.entity.Banner;
import com.magao.backend.module.cms.entity.Category;
import com.magao.backend.module.cms.entity.News;
import com.magao.backend.module.cms.entity.Partner;
import com.magao.backend.module.cms.mapper.BannerMapper;
import com.magao.backend.module.cms.mapper.CategoryMapper;
import com.magao.backend.module.cms.mapper.NewsMapper;
import com.magao.backend.module.cms.mapper.PartnerMapper;
import com.magao.backend.module.classmanage.entity.TeachClass;
import com.magao.backend.module.classmanage.mapper.TeachClassMapper;
import com.magao.backend.module.competition.entity.CompetitionInfo;
import com.magao.backend.module.competition.entity.CompetitionParticipant;
import com.magao.backend.module.competition.mapper.CompetitionInfoMapper;
import com.magao.backend.module.competition.mapper.CompetitionParticipantMapper;
import com.magao.backend.module.course.entity.Course;
import com.magao.backend.module.course.mapper.CourseMapper;
import com.magao.backend.module.exam.entity.ExamPaper;
import com.magao.backend.module.exam.entity.ExamQuestion;
import com.magao.backend.module.exam.entity.ExamRecord;
import com.magao.backend.module.exam.mapper.ExamPaperMapper;
import com.magao.backend.module.exam.mapper.ExamQuestionMapper;
import com.magao.backend.module.exam.mapper.ExamRecordMapper;
import com.magao.backend.module.file.entity.FileRecord;
import com.magao.backend.module.file.mapper.FileRecordMapper;
import com.magao.backend.module.marketing.entity.Coupon;
import com.magao.backend.module.marketing.entity.Membership;
import com.magao.backend.module.marketing.mapper.CouponMapper;
import com.magao.backend.module.marketing.mapper.MembershipMapper;
import com.magao.backend.module.practice.entity.PracticeQuestion;
import com.magao.backend.module.practice.mapper.PracticeQuestionMapper;
import com.magao.backend.module.system.entity.SysConfig;
import com.magao.backend.module.system.entity.SysNotice;
import com.magao.backend.module.system.mapper.SysConfigMapper;
import com.magao.backend.module.system.mapper.SysNoticeMapper;
import com.magao.backend.module.system.service.SystemService;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理聚合控制器（对齐前端 /admin/* 契约）
 * <p>
 * 复用各业务模块既有 Mapper/表，集中暴露管理端列表与统计接口，
 * 消除前端 admin 页调用 /admin/* 时的 404 漂移。写操作（增改删）
 * 前端 useCrud 已具备本地兜底，后续可按需再补。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "后台管理", description = "学员/教师/账号/课程/班级/赛事/题库/试卷/成绩/练习/营销/文件/资讯/分类/伙伴/通知/统计/设置")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
// 后台聚合接口至少需要一个后台角色，普通学生不得读取管理端数据
@RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER, UserRoleEnum.OPERATOR})
public class AdminController extends BaseController {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final TeachClassMapper teachClassMapper;
    private final CompetitionInfoMapper competitionInfoMapper;
    private final CompetitionParticipantMapper competitionParticipantMapper;
    private final BannerMapper bannerMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamRecordMapper examRecordMapper;
    private final PracticeQuestionMapper practiceQuestionMapper;
    private final CouponMapper couponMapper;
    private final MembershipMapper membershipMapper;
    private final FileRecordMapper fileRecordMapper;
    private final NewsMapper newsMapper;
    private final CategoryMapper categoryMapper;
    private final PartnerMapper partnerMapper;
    private final SysNoticeMapper sysNoticeMapper;
    private final SysConfigMapper sysConfigMapper;
    private final SystemService systemService;

    // ===================== 用户类（sys_user 按角色过滤） =====================

    @Operation(summary = "学员列表")
    @GetMapping("/student/list")
    public Result<Page<User>> studentList(PageDTO pageDTO) {
        return Result.success(userPage(pageDTO, "STUDENT"));
    }

    @Operation(summary = "教师列表")
    @GetMapping("/teacher/list")
    public Result<Page<User>> teacherList(PageDTO pageDTO) {
        return Result.success(userPage(pageDTO, "TEACHER"));
    }

    @Operation(summary = "后台账号列表")
    @GetMapping("/account/list")
    public Result<Page<User>> accountList(PageDTO pageDTO) {
        Page<User> p = userMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()),
                new LambdaQueryWrapper<User>().in(User::getRoleType, "ADMIN", "OPERATOR").orderByDesc(User::getId));
        p.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(p);
    }

    // ===================== 业务资源列表 =====================

    @Operation(summary = "课程列表")
    @GetMapping("/course/list")
    public Result<Page<Course>> courseList(PageDTO pageDTO) {
        return Result.success(courseMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "班级列表")
    @GetMapping("/class/list")
    public Result<Page<TeachClass>> classList(PageDTO pageDTO) {
        return Result.success(teachClassMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "赛事列表")
    @GetMapping("/exam/list")
    public Result<Page<CompetitionInfo>> examList(PageDTO pageDTO) {
        return Result.success(competitionInfoMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "Banner列表")
    @GetMapping("/banner/list")
    public Result<Page<Banner>> bannerList(PageDTO pageDTO) {
        return Result.success(bannerMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "报名列表")
    @GetMapping("/registration/list")
    public Result<Page<CompetitionParticipant>> registrationList(PageDTO pageDTO) {
        return Result.success(competitionParticipantMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "题库列表")
    @GetMapping("/question/list")
    public Result<Page<ExamQuestion>> questionList(PageDTO pageDTO) {
        return Result.success(examQuestionMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "试卷列表")
    @GetMapping("/paper/list")
    public Result<Page<ExamPaper>> paperList(PageDTO pageDTO) {
        return Result.success(examPaperMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "成绩列表")
    @GetMapping("/score/list")
    public Result<Page<ExamRecord>> scoreList(PageDTO pageDTO) {
        return Result.success(examRecordMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "练习列表")
    @GetMapping("/practice/list")
    public Result<Page<PracticeQuestion>> practiceList(PageDTO pageDTO) {
        return Result.success(practiceQuestionMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "营销套餐列表")
    @GetMapping("/marketing/plans")
    public Result<List<Membership>> marketingPlans() {
        return Result.success(membershipMapper.selectList(null));
    }

    @Operation(summary = "优惠券列表")
    @GetMapping("/marketing/coupons")
    public Result<List<Coupon>> marketingCoupons() {
        return Result.success(couponMapper.selectList(null));
    }

    @Operation(summary = "文件列表")
    @GetMapping("/file/list")
    public Result<Page<FileRecord>> fileList(PageDTO pageDTO) {
        return Result.success(fileRecordMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "资讯列表")
    @GetMapping("/news/list")
    public Result<Page<News>> newsList(PageDTO pageDTO) {
        return Result.success(newsMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "分类列表")
    @GetMapping("/category/list")
    public Result<Page<Category>> categoryList(PageDTO pageDTO) {
        return Result.success(categoryMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "合作伙伴列表")
    @GetMapping("/partner/list")
    public Result<Page<Partner>> partnerList(PageDTO pageDTO) {
        return Result.success(partnerMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    @Operation(summary = "通知列表")
    @GetMapping("/notification/list")
    public Result<Page<SysNotice>> notificationList(PageDTO pageDTO) {
        return Result.success(sysNoticeMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()), null));
    }

    // ===================== 统计与设置 =====================

    @Operation(summary = "仪表盘统计")
    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> dashboardStats() {
        Map<String, Object> m = new HashMap<>();
        m.put("studentCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRoleType, "STUDENT")));
        m.put("teacherCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRoleType, "TEACHER")));
        m.put("courseCount", courseMapper.selectCount(null));
        m.put("activeExamCount", competitionInfoMapper.selectCount(new LambdaQueryWrapper<CompetitionInfo>().eq(CompetitionInfo::getStatus, 1)));
        m.put("registrationCount", competitionParticipantMapper.selectCount(null));
        m.put("questionCount", examQuestionMapper.selectCount(null));
        m.put("paperCount", examPaperMapper.selectCount(null));
        m.put("classCount", teachClassMapper.selectCount(null));
        m.put("scoreCount", examRecordMapper.selectCount(null));
        m.put("newsCount", newsMapper.selectCount(null));
        m.put("practiceCount", practiceQuestionMapper.selectCount(null));
        return Result.success(m);
    }

    @Operation(summary = "系统设置（扁平 KV 映射）")
    @GetMapping("/settings")
    @RequireRole({UserRoleEnum.ADMIN})
    public Result<Map<String, Object>> settings() {
        List<SysConfig> all = sysConfigMapper.selectList(null);
        Map<String, Object> map = new LinkedHashMap<>();
        for (SysConfig c : all) {
            map.put(c.getConfigKey(), coerceConfigValue(c.getConfigValue()));
        }
        return Result.success(map);
    }

    @Operation(summary = "保存系统设置（扁平 KV upsert）")
    @PutMapping("/settings")
    @RequireRole({UserRoleEnum.ADMIN})
    public Result<Void> saveSettings(@RequestBody Map<String, Object> body) {
        if (body != null) {
            for (Map.Entry<String, Object> e : body.entrySet()) {
                systemService.setConfigValue(e.getKey(), String.valueOf(e.getValue()));
            }
        }
        return Result.success();
    }

    /** 将 sys_config 的字符串值还原为前端所需类型（布尔/数值/字符串） */
    private Object coerceConfigValue(String v) {
        if (v == null) return null;
        if ("true".equalsIgnoreCase(v)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(v)) return Boolean.FALSE;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ignore) {
            // not an integer, fall through
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException ignore) {
            // not a long, fall through
        }
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException ignore) {
            // not a double, keep as string
        }
        return v;
    }

    // ===================== 工具 =====================

    private Page<User> userPage(PageDTO pageDTO, String role) {
        Page<User> p = userMapper.selectPage(new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize()),
                new LambdaQueryWrapper<User>().eq(User::getRoleType, role).orderByDesc(User::getId));
        p.getRecords().forEach(u -> u.setPassword(null));
        return p;
    }
}
