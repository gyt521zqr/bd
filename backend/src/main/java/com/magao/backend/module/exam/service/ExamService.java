package com.magao.backend.module.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.exam.entity.ExamInfo;
import com.magao.backend.module.exam.entity.ExamRecord;

/**
 * 考试服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface ExamService extends IService<ExamInfo> {

    /** 分页查询考试列表 */
    Page<ExamInfo> pageList(PageDTO pageDTO, Integer examType, Integer status);

    /** 获取考试详情 */
    ExamInfo getDetail(Long examId);

    /** 进入考试 */
    ExamRecord enterExam(Long userId, Long examId);

    /** 提交考试 */
    ExamRecord submitExam(Long userId, Long examId, Long recordId);

    /** 获取考试结果（按考试记录ID查询） */
    ExamRecord getResult(Long userId, Long recordId);

    /** 保存考试答案（currentUserId 用于校验答卷归属，防写到他人答卷） */
    void saveAnswer(Long recordId, Long questionId, String userAnswer, String code, Integer language, Long currentUserId);

    /** 获取考试答案列表 */
    java.util.List<com.magao.backend.module.exam.entity.ExamAnswer> getAnswers(Long recordId);

    /** 我的考试记录（分页，含考试标题/总分/及格分） */
    Page<com.magao.backend.module.exam.vo.ExamRecordVO> listRecords(PageDTO pageDTO, Long userId);

    /** 按参赛号+姓名查询考试成绩（公开） */
    ExamRecord queryResult(String registerNo, String name);

    /** 上报切屏事件 */
    void reportSwitchScreen(Long userId, Long examId);

    /** 批改任务列表（教师） */
    Page<com.magao.backend.module.exam.entity.ExamGradingTask> listGradingTasks(PageDTO pageDTO, Long teacherId);

    /** 批改某题 */
    void gradeQuestion(Long recordId, Long questionId, Integer score);

    /** 自动批改指定考试记录的客观题（供 MQ 消费者调用，已批改的跳过） */
    void autoGradeRecord(Long recordId);

    /**
     * 试卷题目（题库单源）：按试卷ID返回题目列表（含选项数组）。
     * 默认不返回标准答案与解析（答题态防泄露）；仅教师/管理员阅卷预览时 includeAnswer=true。
     */
    java.util.List<com.magao.backend.module.exam.vo.ExamQuestionVO> listPaperQuestions(Long paperId, boolean includeAnswer);

    /** 前台试卷列表（题库中心展示，返回真实试卷，不含答案） */
    java.util.List<com.magao.backend.module.exam.entity.ExamPaper> listPapers();

    /** 逐题答题详情（成绩页用）：校验答卷归属后返回题干+用户答案+标准答案+解析+得分 */
    java.util.List<com.magao.backend.module.exam.vo.ExamResultDetailVO> getResultDetail(Long userId, Long recordId);
}
