package com.magao.backend.module.judge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.judge.dto.*;
import com.magao.backend.module.judge.judge.AbstractJudge;
import com.magao.backend.module.judge.judge.JudgeFactory;
import com.magao.backend.module.practice.entity.PracticeDraft;
import com.magao.backend.module.practice.entity.PracticeRecord;
import com.magao.backend.common.util.TosUtil;
import com.magao.backend.module.practice.mapper.PracticeDraftMapper;
import com.magao.backend.module.practice.mapper.PracticeRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 统一判题控制器
 * <p>
 * 仅暴露单一判题接口 /api/judge/submit，通过 lang 字段分发对应语言判题逻辑。
 * 同时提供运行、草稿保存/加载、结果查询等接口。
 * </p>
 *
 * @author Magao开发团队
 */
@Slf4j
@Tag(name = "统一判题", description = "代码提交、运行、判题结果查询等接口")
@RestController
@RequestMapping("/api/judge")
@RequiredArgsConstructor
public class JudgeController extends BaseController {

    private final JudgeFactory judgeFactory;
    private final PracticeRecordMapper practiceRecordMapper;
    private final PracticeDraftMapper practiceDraftMapper;
    private final ObjectProvider<TosUtil> tosUtilProvider;

    /** Scratch 封面存储桶（与 ScratchWorkServiceImpl 保持一致） */
    private static final String SCRATCH_BUCKET = "scratch-works";
    /** Scratch 封面存储文件夹 */
    private static final String COVER_FOLDER = "cover/";

    @Operation(summary = "提交判题")
    @PostMapping("/submit")
    public Result<Long> submit(@RequestBody @Valid JudgeSubmitDTO submitDTO) {
        Long userId = getCurrentUserId();
        log.info("判题提交：userId={}, questionId={}, lang={}", userId, submitDTO.getQuestionId(), submitDTO.getLang());

        // 获取对应语言的判题实现
        AbstractJudge judge = judgeFactory.getJudge(submitDTO.getLang());

        // 校验提交内容
        String validateError = judge.validate(submitDTO);
        if (validateError != null) {
            return Result.fail(validateError);
        }

        // 保存作答记录到数据库
        PracticeRecord record = new PracticeRecord();
        record.setUserId(userId);
        record.setQuestionId(submitDTO.getQuestionId());
        record.setCode(submitDTO.getCode());
        record.setLanguage(convertLangToInt(submitDTO.getLang()));
        record.setJudgeStatus(1); // 评测中
        // 持久化 Scratch 作品封面（base64 → 对象存储），失败不影响主流程
        record.setCoverUrl(resolveCoverUrl(submitDTO.getScratchCover()));
        practiceRecordMapper.insert(record);

        log.info("判题记录已保存：recordId={}", record.getId());

        // 同步判题（开发阶段，后续可改为 MQ 异步）
        JudgeResultDTO judgeResult = judge.judge(submitDTO);

        // 更新判题结果到数据库
        record.setJudgeStatus(convertStringToJudgeStatus(judgeResult.getStatus()));
        record.setPassCaseCount(judgeResult.getPassedCases());
        record.setTotalCaseCount(judgeResult.getTotalCases());
        if (judgeResult.getTimeUsed() != null) {
            record.setExecuteTime(judgeResult.getTimeUsed().intValue());
        }
        if (judgeResult.getMemoryUsed() != null) {
            record.setMemoryUsage(judgeResult.getMemoryUsed().intValue());
        }
        practiceRecordMapper.updateById(record);

        log.info("判题完成：recordId={}, status={}, score={}", record.getId(), judgeResult.getStatus(), judgeResult.getScore());

        return Result.success(record.getId());
    }

    /**
     * 将前端传来的 Scratch 封面（base64 dataURL）解码并上传到对象存储，返回可访问 URL。
     * 任何异常都会被吞掉并返回 null，确保封面上传失败不影响判题主流程。
     */
    private String resolveCoverUrl(String scratchCover) {
        if (scratchCover == null || scratchCover.isBlank()) {
            return null;
        }
        TosUtil tosUtil = tosUtilProvider.getIfAvailable();
        if (tosUtil == null) {
            log.warn("TOS 未启用，跳过 Scratch 封面持久化");
            return null;
        }
        try {
            String base64;
            int comma = scratchCover.indexOf(',');
            if (scratchCover.startsWith("data:") && comma > 0) {
                base64 = scratchCover.substring(comma + 1);
            } else {
                base64 = scratchCover;
            }
            byte[] bytes = Base64.getDecoder().decode(base64);
            if (bytes.length == 0) {
                return null;
            }
            String objectKey = tosUtil.upload(bytes, "image/png", SCRATCH_BUCKET, COVER_FOLDER);
            return tosUtil.getPublicUrl(objectKey, SCRATCH_BUCKET);
        } catch (Exception e) {
            log.warn("Scratch 封面上传失败，已跳过：{}", e.getMessage());
            return null;
        }
    }

    @Operation(summary = "查询判题结果")
    @GetMapping("/result/{recordId}")
    public Result<JudgeResultDTO> getResult(@PathVariable Long recordId) {
        PracticeRecord record = practiceRecordMapper.selectById(recordId);
        if (record == null) {
            return Result.fail("判题记录不存在");
        }

        JudgeResultDTO result = JudgeResultDTO.builder()
                .recordId(record.getId())
                .status(convertJudgeStatusToString(record.getJudgeStatus()))
                .score(record.getPassCaseCount() != null && record.getTotalCaseCount() != null && record.getTotalCaseCount() > 0
                        ? (record.getPassCaseCount() * 100 / record.getTotalCaseCount()) : 0)
                .totalScore(100)
                .passedCases(record.getPassCaseCount())
                .totalCases(record.getTotalCaseCount())
                .timeUsed(record.getExecuteTime() != null ? record.getExecuteTime().longValue() : null)
                .memoryUsed(record.getMemoryUsage() != null ? record.getMemoryUsage().longValue() : null)
                .coverUrl(record.getCoverUrl())
                .build();
        return Result.success(result);
    }

    @Operation(summary = "运行代码（仅运行不判题）")
    @PostMapping("/run")
    public Result<RunResultDTO> run(@RequestBody @Valid JudgeSubmitDTO submitDTO) {
        log.info("运行代码：userId={}, questionId={}, lang={}", getCurrentUserId(), submitDTO.getQuestionId(), submitDTO.getLang());

        AbstractJudge judge = judgeFactory.getJudge(submitDTO.getLang());
        RunResultDTO result = judge.run(submitDTO);
        return Result.success(result);
    }

    @Operation(summary = "保存草稿")
    @PostMapping("/draft/save")
    public Result<Void> saveDraft(@RequestBody @Valid DraftDTO draftDTO) {
        Long userId = getCurrentUserId();
        log.info("保存草稿：userId={}, questionId={}, lang={}", userId, draftDTO.getQuestionId(), draftDTO.getLang());

        // 查询是否已有草稿，有则更新，无则新增
        LambdaQueryWrapper<PracticeDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeDraft::getUserId, userId)
                .eq(PracticeDraft::getQuestionId, draftDTO.getQuestionId())
                .eq(PracticeDraft::getLanguage, convertLangToInt(draftDTO.getLang()));
        PracticeDraft existingDraft = practiceDraftMapper.selectOne(wrapper);

        if (existingDraft != null) {
            existingDraft.setCode(draftDTO.getCode());
            existingDraft.setUpdateTime(LocalDateTime.now());
            practiceDraftMapper.updateById(existingDraft);
        } else {
            PracticeDraft draft = new PracticeDraft();
            draft.setUserId(userId);
            draft.setQuestionId(draftDTO.getQuestionId());
            draft.setCode(draftDTO.getCode());
            draft.setLanguage(convertLangToInt(draftDTO.getLang()));
            draft.setUpdateTime(LocalDateTime.now());
            practiceDraftMapper.insert(draft);
        }

        return Result.success(null);
    }

    @Operation(summary = "加载草稿")
    @GetMapping("/draft/load")
    public Result<DraftDTO> loadDraft(@RequestParam Long questionId, @RequestParam String lang) {
        Long userId = getCurrentUserId();
        log.info("加载草稿：userId={}, questionId={}, lang={}", userId, questionId, lang);

        LambdaQueryWrapper<PracticeDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeDraft::getUserId, userId)
                .eq(PracticeDraft::getQuestionId, questionId)
                .eq(PracticeDraft::getLanguage, convertLangToInt(lang));
        PracticeDraft draft = practiceDraftMapper.selectOne(wrapper);

        if (draft == null) {
            return Result.success(null);
        }

        DraftDTO draftDTO = new DraftDTO();
        draftDTO.setQuestionId(draft.getQuestionId());
        draftDTO.setLang(convertIntToLang(draft.getLanguage()));
        draftDTO.setCode(draft.getCode());
        return Result.success(draftDTO);
    }

    @Operation(summary = "获取判题记录列表")
    @GetMapping("/records")
    public Result<Object> getRecords(@RequestParam Long questionId,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();

        Page<PracticeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeRecord::getUserId, userId)
                .eq(PracticeRecord::getQuestionId, questionId)
                .orderByDesc(PracticeRecord::getId);
        Page<PracticeRecord> resultPage = practiceRecordMapper.selectPage(page, wrapper);

        return Result.success(resultPage);
    }

    /**
     * 将语言标识字符串转换为整数编码
     */
    private Integer convertLangToInt(String lang) {
        if (lang == null) { return 1; }
        return switch (lang) {
            case "python" -> 1;
            case "cpp" -> 2;
            case "goc" -> 3;
            case "scratch3" -> 4;
            case "scratchjr" -> 5;
            case "pythonturtle" -> 6;
            default -> 1;
        };
    }

    /**
     * 将整数编码转换为语言标识字符串
     */
    private String convertIntToLang(Integer language) {
        if (language == null) { return "python"; }
        return switch (language) {
            case 1 -> "python";
            case 2 -> "cpp";
            case 3 -> "goc";
            case 4 -> "scratch3";
            case 5 -> "scratchjr";
            case 6 -> "pythonturtle";
            default -> "python";
        };
    }

    /**
     * 将判题状态整数编码转换为字符串
     */
    private String convertJudgeStatusToString(Integer judgeStatus) {
        if (judgeStatus == null) { return "PENDING"; }
        return switch (judgeStatus) {
            case 0 -> "PENDING";
            case 1 -> "RUNNING";
            case 2 -> "ACCEPTED";
            case 3 -> "WRONG_ANSWER";
            case 4 -> "TIME_LIMIT_EXCEEDED";
            case 5 -> "MEMORY_LIMIT_EXCEEDED";
            case 6 -> "RUNTIME_ERROR";
            case 7 -> "COMPILE_ERROR";
            default -> "SYSTEM_ERROR";
        };
    }

    private Integer convertStringToJudgeStatus(String status) {
        if (status == null) { return 0; }
        return switch (status) {
            case "PENDING" -> 0;
            case "RUNNING" -> 1;
            case "ACCEPTED" -> 2;
            case "WRONG_ANSWER" -> 3;
            case "TIME_LIMIT_EXCEEDED" -> 4;
            case "MEMORY_LIMIT_EXCEEDED" -> 5;
            case "RUNTIME_ERROR" -> 6;
            case "COMPILE_ERROR" -> 7;
            default -> 8;
        };
    }
}
