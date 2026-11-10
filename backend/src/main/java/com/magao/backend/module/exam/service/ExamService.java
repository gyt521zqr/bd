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

    /** 保存考试答案 */
    void saveAnswer(Long recordId, Long questionId, String userAnswer, String code, Integer language);

    /** 获取考试答案列表 */
    java.util.List<com.magao.backend.module.exam.entity.ExamAnswer> getAnswers(Long recordId);

    /** 我的考试记录（分页） */
    Page<ExamRecord> listRecords(PageDTO pageDTO, Long userId);

    /** 按参赛号+姓名查询考试成绩（公开） */
    ExamRecord queryResult(String registerNo, String name);

    /** 上报切屏事件 */
    void reportSwitchScreen(Long userId, Long examId);

    /** 批改任务列表（教师） */
    Page<com.magao.backend.module.exam.entity.ExamGradingTask> listGradingTasks(PageDTO pageDTO, Long teacherId);

    /** 批改某题 */
    void gradeQuestion(Long recordId, Long questionId, Integer score);

    /** 试卷题目（题库单源）：按试卷ID返回题目列表（含选项数组） */
    java.util.List<com.magao.backend.module.exam.vo.ExamQuestionVO> listPaperQuestions(Long paperId);
}
