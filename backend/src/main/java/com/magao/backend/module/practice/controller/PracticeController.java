package com.magao.backend.module.practice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.practice.entity.PracticeQuestion;
import com.magao.backend.module.practice.entity.PracticeRecord;
import com.magao.backend.module.practice.mapper.PracticeRecordMapper;
import com.magao.backend.module.practice.service.PracticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 练习管理控制器
 * <p>
 * 提供练习题相关的API接口
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "练习管理", description = "练习题查询、提交等接口")
@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController extends BaseController {

    private final PracticeService practiceService;
    private final PracticeRecordMapper practiceRecordMapper;

    @Operation(summary = "练习题列表")
    @GetMapping("/list")
    public Result<Page<PracticeQuestion>> list(PageDTO pageDTO,
                                                @RequestParam(required = false) Integer language,
                                                @RequestParam(required = false) Integer difficulty) {
        return Result.success(practiceService.pageList(pageDTO, language, difficulty));
    }

    @Operation(summary = "练习题详情")
    @GetMapping("/{id}")
    public Result<PracticeQuestion> detail(@PathVariable Long id) {
        return Result.success(practiceService.getDetail(id));
    }

    @Operation(summary = "练习题详情（段序别名 /detail/{id}）")
    @GetMapping("/detail/{id}")
    public Result<PracticeQuestion> detailAlias(@PathVariable Long id) {
        return Result.success(practiceService.getDetail(id));
    }

    @Operation(summary = "提交代码评测")
    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestBody SubmitReq req) {
        if (req.getQuestionId() == null || req.getLanguage() == null || req.getCode() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "题目ID、语言、代码均不能为空");
        }
        PracticeRecord record = practiceService.submit(getCurrentUserId(), req.getQuestionId(), req.getCode(), req.getLanguage());
        return Result.success(toSubmitResult(record));
    }

    @Operation(summary = "获取提交记录（当前用户）")
    @GetMapping("/records")
    public Result<Page<PracticeRecord>> records(PageDTO pageDTO,
                                                @RequestParam(required = false) Long questionId) {
        Page<PracticeRecord> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<PracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeRecord::getUserId, getCurrentUserId());
        if (questionId != null) {
            wrapper.eq(PracticeRecord::getQuestionId, questionId);
        }
        wrapper.orderByDesc(PracticeRecord::getCreateTime);
        return Result.success(practiceRecordMapper.selectPage(page, wrapper));
    }

    @Operation(summary = "获取提交结果详情（轮询）")
    @GetMapping("/result/{id}")
    public Result<Map<String, Object>> result(@PathVariable Long id) {
        PracticeRecord record = practiceRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        // 仅本人可查看自己的提交结果
        if (!Objects.equals(record.getUserId(), getCurrentUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return Result.success(toSubmitResult(record));
    }

    @Operation(summary = "获取题目标签列表")
    @GetMapping("/tags")
    public Result<List<String>> tags() {
        // 练习题库未单独维护 tags 列，返回一套与课程体系匹配的常用标签，供前端筛选
        return Result.success(Arrays.asList(
                "Python基础", "C++入门", "GoC图形化", "Scratch创意", "算法入门", "数据结构", "逻辑思维", "竞赛真题"));
    }

    /** 将练习记录转换为前端 SubmitResult 形状 */
    private Map<String, Object> toSubmitResult(PracticeRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("practiceId", r.getQuestionId());
        m.put("userId", r.getUserId());
        m.put("code", r.getCode());
        m.put("language", r.getLanguage());
        m.put("status", mapStatus(r.getJudgeStatus()));
        m.put("score", r.getPassCaseCount() == null ? 0 : r.getPassCaseCount());
        m.put("submitTime", r.getCreateTime() == null ? null : r.getCreateTime().toString());
        m.put("runTime", r.getExecuteTime() == null ? 0 : r.getExecuteTime());
        return m;
    }

    private String mapStatus(Integer judgeStatus) {
        if (judgeStatus == null) return "pending";
        if (judgeStatus == 2) return "accepted";
        if (judgeStatus >= 3) return "wrong_answer";
        return "pending";
    }

    /** 提交代码请求体（JSON，对齐前端 submitCode） */
    public static class SubmitReq {
        private Long questionId;
        private Integer language;
        private String code;
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Integer getLanguage() { return language; }
        public void setLanguage(Integer language) { this.language = language; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
