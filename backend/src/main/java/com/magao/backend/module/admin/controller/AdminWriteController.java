package com.magao.backend.module.admin.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.common.base.BaseEntity;
import com.magao.backend.common.result.Result;
import com.magao.backend.common.util.CryptoUtil;
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
import com.magao.backend.module.system.entity.SysNotice;
import com.magao.backend.module.system.mapper.SysNoticeMapper;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 后台管理写操作控制器（对齐前端 useCrud 契约）。
 * <p>
 * useCrud 约定的写接口形态（apiBase 例如 /admin/course）：
 * - POST   {apiBase}/create       新建（body 为表单对象，返回 data=新记录含 id）
 * - PUT    {apiBase}/{id}          更新（body 为表单对象）
 * - DELETE {apiBase}/{id}          删除（逻辑删除，依赖实体 @TableLogic）
 * - POST   {apiBase}/batch-delete  批量删除（body {ids:[...]}）
 * 与 {@link AdminController} 的列表/统计端点共用 /api/admin 基址，互不冲突。
 * 用户类（student/teacher/account）写操作已承载：统一写 sys_user 表，按路径决定
 * roleType（STUDENT/TEACHER），account 由 DTO 的 roleType 决定（ADMIN/OPERATOR）；
 * 密码用与登录一致的盐值 {@code CryptoUtil.md5WithSalt(pwd, "magao_salt_2024")} 哈希，
 * 确保新建/重置的账号可被 AuthServiceImpl 正常登录校验。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "后台管理-写操作", description = "课程/班级/赛事/报名/题库/试卷/成绩/练习/文件/Banner/资讯/分类/伙伴/通知/营销 的增删改")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminWriteController {

    private final CourseMapper courseMapper;
    private final TeachClassMapper teachClassMapper;
    private final CompetitionInfoMapper competitionInfoMapper;
    private final CompetitionParticipantMapper competitionParticipantMapper;
    private final BannerMapper bannerMapper;
    private final NewsMapper newsMapper;
    private final CategoryMapper categoryMapper;
    private final PartnerMapper partnerMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamRecordMapper examRecordMapper;
    private final PracticeQuestionMapper practiceQuestionMapper;
    private final FileRecordMapper fileRecordMapper;
    private final SysNoticeMapper sysNoticeMapper;
    private final MembershipMapper membershipMapper;
    private final CouponMapper couponMapper;
    private final UserMapper userMapper;

    // ===================== 课程 =====================
    @Operation(summary = "新建课程")
    @PostMapping("/course/create")
    public Result<Course> createCourse(@RequestBody Course e) {
        return doCreate(courseMapper, e);
    }

    @Operation(summary = "更新课程")
    @PutMapping("/course/{id}")
    public Result<Course> updateCourse(@PathVariable Long id, @RequestBody Course e) {
        return doUpdate(courseMapper, id, e);
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/course/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        return doDelete(courseMapper, id);
    }

    @Operation(summary = "批量删除课程")
    @PostMapping("/course/batch-delete")
    public Result<Void> batchDeleteCourse(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(courseMapper, body.get("ids"));
    }

    // ===================== 班级 =====================
    @Operation(summary = "新建班级")
    @PostMapping("/class/create")
    public Result<TeachClass> createClass(@RequestBody TeachClass e) {
        return doCreate(teachClassMapper, e);
    }

    @Operation(summary = "更新班级")
    @PutMapping("/class/{id}")
    public Result<TeachClass> updateClass(@PathVariable Long id, @RequestBody TeachClass e) {
        return doUpdate(teachClassMapper, id, e);
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/class/{id}")
    public Result<Void> deleteClass(@PathVariable Long id) {
        return doDelete(teachClassMapper, id);
    }

    @Operation(summary = "批量删除班级")
    @PostMapping("/class/batch-delete")
    public Result<Void> batchDeleteClass(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(teachClassMapper, body.get("ids"));
    }

    // ===================== 赛事（竞赛） =====================
    @Operation(summary = "新建赛事")
    @PostMapping("/exam/create")
    public Result<CompetitionInfo> createExam(@RequestBody CompetitionInfo e) {
        return doCreate(competitionInfoMapper, e);
    }

    @Operation(summary = "更新赛事")
    @PutMapping("/exam/{id}")
    public Result<CompetitionInfo> updateExam(@PathVariable Long id, @RequestBody CompetitionInfo e) {
        return doUpdate(competitionInfoMapper, id, e);
    }

    @Operation(summary = "删除赛事")
    @DeleteMapping("/exam/{id}")
    public Result<Void> deleteExam(@PathVariable Long id) {
        return doDelete(competitionInfoMapper, id);
    }

    @Operation(summary = "批量删除赛事")
    @PostMapping("/exam/batch-delete")
    public Result<Void> batchDeleteExam(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(competitionInfoMapper, body.get("ids"));
    }

    // ===================== 报名 =====================
    @Operation(summary = "新建报名")
    @PostMapping("/registration/create")
    public Result<CompetitionParticipant> createRegistration(@RequestBody CompetitionParticipant e) {
        return doCreate(competitionParticipantMapper, e);
    }

    @Operation(summary = "更新报名")
    @PutMapping("/registration/{id}")
    public Result<CompetitionParticipant> updateRegistration(@PathVariable Long id, @RequestBody CompetitionParticipant e) {
        return doUpdate(competitionParticipantMapper, id, e);
    }

    @Operation(summary = "删除报名")
    @DeleteMapping("/registration/{id}")
    public Result<Void> deleteRegistration(@PathVariable Long id) {
        return doDelete(competitionParticipantMapper, id);
    }

    @Operation(summary = "批量删除报名")
    @PostMapping("/registration/batch-delete")
    public Result<Void> batchDeleteRegistration(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(competitionParticipantMapper, body.get("ids"));
    }

    // ===================== Banner =====================
    @Operation(summary = "新建Banner")
    @PostMapping("/banner/create")
    public Result<Banner> createBanner(@RequestBody Banner e) {
        return doCreate(bannerMapper, e);
    }

    @Operation(summary = "更新Banner")
    @PutMapping("/banner/{id}")
    public Result<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner e) {
        return doUpdate(bannerMapper, id, e);
    }

    @Operation(summary = "删除Banner")
    @DeleteMapping("/banner/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        return doDelete(bannerMapper, id);
    }

    @Operation(summary = "批量删除Banner")
    @PostMapping("/banner/batch-delete")
    public Result<Void> batchDeleteBanner(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(bannerMapper, body.get("ids"));
    }

    // ===================== 资讯 =====================
    @Operation(summary = "新建资讯")
    @PostMapping("/news/create")
    public Result<News> createNews(@RequestBody News e) {
        return doCreate(newsMapper, e);
    }

    @Operation(summary = "更新资讯")
    @PutMapping("/news/{id}")
    public Result<News> updateNews(@PathVariable Long id, @RequestBody News e) {
        return doUpdate(newsMapper, id, e);
    }

    @Operation(summary = "删除资讯")
    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        return doDelete(newsMapper, id);
    }

    @Operation(summary = "批量删除资讯")
    @PostMapping("/news/batch-delete")
    public Result<Void> batchDeleteNews(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(newsMapper, body.get("ids"));
    }

    // ===================== 分类 =====================
    @Operation(summary = "新建分类")
    @PostMapping("/category/create")
    public Result<Category> createCategory(@RequestBody Category e) {
        return doCreate(categoryMapper, e);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/category/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody Category e) {
        return doUpdate(categoryMapper, id, e);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        return doDelete(categoryMapper, id);
    }

    @Operation(summary = "批量删除分类")
    @PostMapping("/category/batch-delete")
    public Result<Void> batchDeleteCategory(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(categoryMapper, body.get("ids"));
    }

    // ===================== 合作伙伴 =====================
    @Operation(summary = "新建合作伙伴")
    @PostMapping("/partner/create")
    public Result<Partner> createPartner(@RequestBody Partner e) {
        return doCreate(partnerMapper, e);
    }

    @Operation(summary = "更新合作伙伴")
    @PutMapping("/partner/{id}")
    public Result<Partner> updatePartner(@PathVariable Long id, @RequestBody Partner e) {
        return doUpdate(partnerMapper, id, e);
    }

    @Operation(summary = "删除合作伙伴")
    @DeleteMapping("/partner/{id}")
    public Result<Void> deletePartner(@PathVariable Long id) {
        return doDelete(partnerMapper, id);
    }

    @Operation(summary = "批量删除合作伙伴")
    @PostMapping("/partner/batch-delete")
    public Result<Void> batchDeletePartner(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(partnerMapper, body.get("ids"));
    }

    // ===================== 通知 =====================
    @Operation(summary = "新建通知")
    @PostMapping("/notification/create")
    public Result<SysNotice> createNotification(@RequestBody SysNotice e) {
        return doCreate(sysNoticeMapper, e);
    }

    @Operation(summary = "更新通知")
    @PutMapping("/notification/{id}")
    public Result<SysNotice> updateNotification(@PathVariable Long id, @RequestBody SysNotice e) {
        return doUpdate(sysNoticeMapper, id, e);
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/notification/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        return doDelete(sysNoticeMapper, id);
    }

    @Operation(summary = "批量删除通知")
    @PostMapping("/notification/batch-delete")
    public Result<Void> batchDeleteNotification(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(sysNoticeMapper, body.get("ids"));
    }

    // ===================== 题库 =====================
    @Operation(summary = "新建题目")
    @PostMapping("/question/create")
    public Result<ExamQuestion> createQuestion(@RequestBody ExamQuestion e) {
        return doCreate(examQuestionMapper, e);
    }

    @Operation(summary = "更新题目")
    @PutMapping("/question/{id}")
    public Result<ExamQuestion> updateQuestion(@PathVariable Long id, @RequestBody ExamQuestion e) {
        return doUpdate(examQuestionMapper, id, e);
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/question/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        return doDelete(examQuestionMapper, id);
    }

    @Operation(summary = "批量删除题目")
    @PostMapping("/question/batch-delete")
    public Result<Void> batchDeleteQuestion(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(examQuestionMapper, body.get("ids"));
    }

    // ===================== 试卷 =====================
    @Operation(summary = "新建试卷")
    @PostMapping("/paper/create")
    public Result<ExamPaper> createPaper(@RequestBody ExamPaper e) {
        return doCreate(examPaperMapper, e);
    }

    @Operation(summary = "更新试卷")
    @PutMapping("/paper/{id}")
    public Result<ExamPaper> updatePaper(@PathVariable Long id, @RequestBody ExamPaper e) {
        return doUpdate(examPaperMapper, id, e);
    }

    @Operation(summary = "删除试卷")
    @DeleteMapping("/paper/{id}")
    public Result<Void> deletePaper(@PathVariable Long id) {
        return doDelete(examPaperMapper, id);
    }

    @Operation(summary = "批量删除试卷")
    @PostMapping("/paper/batch-delete")
    public Result<Void> batchDeletePaper(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(examPaperMapper, body.get("ids"));
    }

    // ===================== 成绩 =====================
    @Operation(summary = "新建成绩记录")
    @PostMapping("/score/create")
    public Result<ExamRecord> createScore(@RequestBody ExamRecord e) {
        return doCreate(examRecordMapper, e);
    }

    @Operation(summary = "更新成绩记录")
    @PutMapping("/score/{id}")
    public Result<ExamRecord> updateScore(@PathVariable Long id, @RequestBody ExamRecord e) {
        return doUpdate(examRecordMapper, id, e);
    }

    @Operation(summary = "删除成绩记录")
    @DeleteMapping("/score/{id}")
    public Result<Void> deleteScore(@PathVariable Long id) {
        return doDelete(examRecordMapper, id);
    }

    @Operation(summary = "批量删除成绩记录")
    @PostMapping("/score/batch-delete")
    public Result<Void> batchDeleteScore(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(examRecordMapper, body.get("ids"));
    }

    // ===================== 练习 =====================
    @Operation(summary = "新建练习题目")
    @PostMapping("/practice/create")
    public Result<PracticeQuestion> createPractice(@RequestBody PracticeQuestion e) {
        return doCreate(practiceQuestionMapper, e);
    }

    @Operation(summary = "更新练习题目")
    @PutMapping("/practice/{id}")
    public Result<PracticeQuestion> updatePractice(@PathVariable Long id, @RequestBody PracticeQuestion e) {
        return doUpdate(practiceQuestionMapper, id, e);
    }

    @Operation(summary = "删除练习题目")
    @DeleteMapping("/practice/{id}")
    public Result<Void> deletePractice(@PathVariable Long id) {
        return doDelete(practiceQuestionMapper, id);
    }

    @Operation(summary = "批量删除练习题目")
    @PostMapping("/practice/batch-delete")
    public Result<Void> batchDeletePractice(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(practiceQuestionMapper, body.get("ids"));
    }

    // ===================== 文件 =====================
    @Operation(summary = "新建文件记录")
    @PostMapping("/file/create")
    public Result<FileRecord> createFile(@RequestBody FileRecord e) {
        return doCreate(fileRecordMapper, e);
    }

    @Operation(summary = "更新文件记录")
    @PutMapping("/file/{id}")
    public Result<FileRecord> updateFile(@PathVariable Long id, @RequestBody FileRecord e) {
        return doUpdate(fileRecordMapper, id, e);
    }

    @Operation(summary = "删除文件记录")
    @DeleteMapping("/file/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        return doDelete(fileRecordMapper, id);
    }

    @Operation(summary = "批量删除文件记录")
    @PostMapping("/file/batch-delete")
    public Result<Void> batchDeleteFile(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(fileRecordMapper, body.get("ids"));
    }

    // ===================== 营销套餐（literal，路径含斜杠） =====================
    @Operation(summary = "新建营销套餐")
    @PostMapping("/marketing/plans/create")
    public Result<Membership> createPlan(@RequestBody Membership e) {
        return doCreate(membershipMapper, e);
    }

    @Operation(summary = "更新营销套餐")
    @PutMapping("/marketing/plans/{id}")
    public Result<Membership> updatePlan(@PathVariable Long id, @RequestBody Membership e) {
        return doUpdate(membershipMapper, id, e);
    }

    @Operation(summary = "删除营销套餐")
    @DeleteMapping("/marketing/plans/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        return doDelete(membershipMapper, id);
    }

    @Operation(summary = "批量删除营销套餐")
    @PostMapping("/marketing/plans/batch-delete")
    public Result<Void> batchDeletePlan(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(membershipMapper, body.get("ids"));
    }

    // ===================== 优惠券（literal，路径含斜杠） =====================
    @Operation(summary = "新建优惠券")
    @PostMapping("/marketing/coupons/create")
    public Result<Coupon> createCoupon(@RequestBody Coupon e) {
        return doCreate(couponMapper, e);
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/marketing/coupons/{id}")
    public Result<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon e) {
        return doUpdate(couponMapper, id, e);
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/marketing/coupons/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        return doDelete(couponMapper, id);
    }

    @Operation(summary = "批量删除优惠券")
    @PostMapping("/marketing/coupons/batch-delete")
    public Result<Void> batchDeleteCoupon(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(couponMapper, body.get("ids"));
    }

    // ===================== 用户类（student/teacher/account 共用 sys_user） =====================
    /** 与 AuthServiceImpl.PASSWORD_SALT 保持一致，确保新账号登录互通 */
    private static final String PASSWORD_SALT = "magao_salt_2024";

    /**
     * 由 DTO 组装 User。
     * 仅写入非空的 DTO 字段，避免「仅切换状态 / 仅重置密码」这类局部更新把昵称、手机号等
     * 其它列一并置空（MyBatis-Plus updateById 会覆盖实体所有非 null 字段）。
     * roleType 缺省时使用 defaultRole。
     */
    private User buildUser(AdminUserWriteDTO d, String defaultRole) {
        User u = new User();
        u.setId(d.getId());
        if (d.getUsername() != null) u.setUsername(d.getUsername());
        if (d.getNickname() != null) u.setNickname(d.getNickname());
        if (d.getRealName() != null) u.setRealName(d.getRealName());
        if (d.getPhone() != null) u.setPhone(d.getPhone());
        if (d.getEmail() != null) u.setEmail(d.getEmail());
        if (d.getAvatar() != null) u.setAvatar(d.getAvatar());
        if (d.getStatus() != null) u.setStatus(d.getStatus());
        u.setRoleType(d.getRoleType() != null ? d.getRoleType() : defaultRole);
        return u;
    }

    /** 新建：明文密码哈希落库；密码为空则使用默认口令 123456 哈希，保证可登录 */
    private void applyCreatePassword(User u, AdminUserWriteDTO d) {
        String pwd = (d.getPassword() != null && !d.getPassword().isBlank()) ? d.getPassword() : "123456";
        u.setPassword(CryptoUtil.md5WithSalt(pwd, PASSWORD_SALT));
    }

    /** 更新：仅当传入非空密码时才重算哈希，否则保持原密码不变 */
    private void applyUpdatePassword(User u, AdminUserWriteDTO d) {
        if (d.getPassword() != null && !d.getPassword().isBlank()) {
            u.setPassword(CryptoUtil.md5WithSalt(d.getPassword(), PASSWORD_SALT));
        }
    }

    // ----- 学员 -----
    @Operation(summary = "新建学员")
    @PostMapping("/student/create")
    public Result<User> createStudent(@RequestBody AdminUserWriteDTO d) {
        User u = buildUser(d, "STUDENT");
        applyCreatePassword(u, d);
        return doCreate(userMapper, u);
    }

    @Operation(summary = "更新学员")
    @PutMapping("/student/{id}")
    public Result<User> updateStudent(@PathVariable Long id, @RequestBody AdminUserWriteDTO d) {
        User u = buildUser(d, "STUDENT");
        u.setId(id);
        applyUpdatePassword(u, d);
        return doUpdate(userMapper, id, u);
    }

    @Operation(summary = "删除学员")
    @DeleteMapping("/student/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        return doDelete(userMapper, id);
    }

    @Operation(summary = "批量删除学员")
    @PostMapping("/student/batch-delete")
    public Result<Void> batchDeleteStudent(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(userMapper, body.get("ids"));
    }

    // ----- 教师 -----
    @Operation(summary = "新建教师")
    @PostMapping("/teacher/create")
    public Result<User> createTeacher(@RequestBody AdminUserWriteDTO d) {
        User u = buildUser(d, "TEACHER");
        applyCreatePassword(u, d);
        return doCreate(userMapper, u);
    }

    @Operation(summary = "更新教师")
    @PutMapping("/teacher/{id}")
    public Result<User> updateTeacher(@PathVariable Long id, @RequestBody AdminUserWriteDTO d) {
        User u = buildUser(d, "TEACHER");
        u.setId(id);
        applyUpdatePassword(u, d);
        return doUpdate(userMapper, id, u);
    }

    @Operation(summary = "删除教师")
    @DeleteMapping("/teacher/{id}")
    public Result<Void> deleteTeacher(@PathVariable Long id) {
        return doDelete(userMapper, id);
    }

    @Operation(summary = "批量删除教师")
    @PostMapping("/teacher/batch-delete")
    public Result<Void> batchDeleteTeacher(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(userMapper, body.get("ids"));
    }

    // ----- 后台账号 -----
    @Operation(summary = "新建后台账号")
    @PostMapping("/account/create")
    public Result<User> createAccount(@RequestBody AdminUserWriteDTO d) {
        User u = buildUser(d, "ADMIN");
        applyCreatePassword(u, d);
        return doCreate(userMapper, u);
    }

    @Operation(summary = "更新后台账号")
    @PutMapping("/account/{id}")
    public Result<User> updateAccount(@PathVariable Long id, @RequestBody AdminUserWriteDTO d) {
        User u = buildUser(d, "ADMIN");
        u.setId(id);
        applyUpdatePassword(u, d);
        return doUpdate(userMapper, id, u);
    }

    @Operation(summary = "删除后台账号")
    @DeleteMapping("/account/{id}")
    public Result<Void> deleteAccount(@PathVariable Long id) {
        return doDelete(userMapper, id);
    }

    @Operation(summary = "批量删除后台账号")
    @PostMapping("/account/batch-delete")
    public Result<Void> batchDeleteAccount(@RequestBody Map<String, List<Long>> body) {
        return doBatchDelete(userMapper, body.get("ids"));
    }

    // ===================== 泛型 helper =====================

    private <T extends BaseEntity> Result<T> doCreate(BaseMapper<T> mapper, T entity) {
        // 清空自动管理字段，交由数据库默认值/触发器处理
        entity.setId(null);
        entity.setCreateTime(null);
        entity.setUpdateTime(null);
        entity.setDeleted(0);
        mapper.insert(entity);
        return Result.success(entity);
    }

    private <T extends BaseEntity> Result<T> doUpdate(BaseMapper<T> mapper, Long id, T entity) {
        entity.setId(id);
        mapper.updateById(entity);
        return Result.success(entity);
    }

    private <T> Result<Void> doDelete(BaseMapper<T> mapper, Long id) {
        mapper.deleteById(id);
        return Result.success();
    }

    private <T> Result<Void> doBatchDelete(BaseMapper<T> mapper, List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            mapper.deleteBatchIds(ids);
        }
        return Result.success();
    }
}
