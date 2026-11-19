package com.magao.backend.module.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.exam.dto.ExamAnswerSaveDTO;
import com.magao.backend.module.exam.dto.ExamGradeDTO;
import com.magao.backend.module.exam.entity.ExamInfo;
import com.magao.backend.module.exam.entity.ExamPaper;
import com.magao.backend.module.exam.entity.ExamRecord;
import com.magao.backend.module.exam.entity.ExamGradingTask;
import com.magao.backend.module.exam.service.ExamService;
import com.magao.backend.module.exam.vo.ExamQuestionVO;
import com.magao.backend.module.exam.vo.ExamRecordVO;
import com.magao.backend.module.exam.vo.ExamResultDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试管理控制器
 * <p>
 * 提供考试相关的API接口：列表、详情、进入考试、提交、结果
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "考试管理", description = "考试查询、进入、提交等接口")
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController extends BaseController {

    private final ExamService examService;

    @Operation(summary = "考试列表")
    @GetMapping("/list")
    public Result<Page<ExamInfo>> list(PageDTO pageDTO,
                                       @RequestParam(required = false) Integer examType,
                                       @RequestParam(required = false) Integer status) {
        return Result.success(examService.pageList(pageDTO, examType, status));
    }

    @Operation(summary = "考试详情")
    @GetMapping("/{id}")
    public Result<ExamInfo> detail(@PathVariable Long id) {
        return Result.success(examService.getDetail(id));
    }

    @Operation(summary = "进入考试")
    @PostMapping("/{id}/enter")
    public Result<ExamRecord> enter(@PathVariable Long id) {
        return Result.success(examService.enterExam(getCurrentUserId(), id));
    }

    @Operation(summary = "保存考试答案")
    @PostMapping("/{id}/answer")
    public Result<Void> saveAnswer(@PathVariable Long id,
                                   @RequestParam Long recordId,
                                   @RequestBody List<ExamAnswerSaveDTO> answers) {
        if (answers != null) {
            for (ExamAnswerSaveDTO answer : answers) {
                examService.saveAnswer(recordId, answer.getQuestionId(),
                        answer.getUserAnswer(), answer.getCode(), answer.getLanguage(), getCurrentUserId());
            }
        }
        return Result.success(null);
    }

    @Operation(summary = "提交考试")
    @PostMapping("/{id}/submit")
    public Result<ExamRecord> submit(@PathVariable Long id, @RequestParam Long recordId) {
        return Result.success(examService.submitExam(getCurrentUserId(), id, recordId));
    }

    @Operation(summary = "考试结果")
    @GetMapping("/result/{id}")
    public Result<ExamRecord> result(@PathVariable Long id) {
        return Result.success(examService.getResult(getCurrentUserId(), id));
    }

    @Operation(summary = "我的考试记录")
    @GetMapping("/records")
    public Result<Page<ExamRecordVO>> records(PageDTO pageDTO) {
        return Result.success(examService.listRecords(pageDTO, getCurrentUserId()));
    }

    @Operation(summary = "成绩查询（参赛号+姓名）")
    @GetMapping("/result/query")
    public Result<ExamRecord> resultQuery(@RequestParam String registerNo, @RequestParam String name) {
        return Result.success(examService.queryResult(registerNo, name));
    }

    @Operation(summary = "上报切屏事件")
    @PostMapping("/{id}/switch-screen")
    public Result<Void> switchScreen(@PathVariable Long id) {
        examService.reportSwitchScreen(getCurrentUserId(), id);
        return Result.success();
    }

    @Operation(summary = "批改任务列表")
    @GetMapping("/grading/tasks")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    public Result<Page<ExamGradingTask>> gradingTasks(PageDTO pageDTO) {
        return Result.success(examService.listGradingTasks(pageDTO, getCurrentUserId()));
    }

    @Operation(summary = "批改试卷")
    @PostMapping("/grading/grade")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    public Result<Void> grade(@RequestBody ExamGradeDTO dto) {
        examService.gradeQuestion(dto.getRecordId(), dto.getQuestionId(), dto.getScore());
        return Result.success();
    }

    /** 答题态拉题：只返回题干/选项，绝不含标准答案与解析（防抓包作弊） */
    @Operation(summary = "试卷题目（答题态，无答案）")
    @GetMapping("/paper/{paperId}/questions")
    public Result<List<ExamQuestionVO>> paperQuestions(@PathVariable Long paperId) {
        return Result.success(examService.listPaperQuestions(paperId, false));
    }

    /** 教师阅卷预览：含标准答案与解析，仅管理员/教师可访问 */
    @Operation(summary = "试卷题目（阅卷预览，含答案）")
    @GetMapping("/paper/{paperId}/questions-with-answers")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    public Result<List<ExamQuestionVO>> paperQuestionsWithAnswers(@PathVariable Long paperId) {
        return Result.success(examService.listPaperQuestions(paperId, true));
    }

    /** 前台试卷列表（题库中心）：返回真实试卷，公开只读 */
    @Operation(summary = "试卷列表（题库中心）")
    @GetMapping("/paper/list")
    public Result<List<ExamPaper>> paperList() {
        return Result.success(examService.listPapers());
    }

    /** 逐题答题详情（成绩页）：仅本人可查，含题干+用户答案+标准答案+解析+得分 */
    @Operation(summary = "考试答题详情（成绩页）")
    @GetMapping("/result/{id}/detail")
    public Result<List<ExamResultDetailVO>> resultDetail(@PathVariable Long id) {
        return Result.success(examService.getResultDetail(getCurrentUserId(), id));
    }
}
