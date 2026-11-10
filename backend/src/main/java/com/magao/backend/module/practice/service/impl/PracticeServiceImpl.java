package com.magao.backend.module.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.judge.dto.JudgeResultDTO;
import com.magao.backend.module.judge.dto.JudgeSubmitDTO;
import com.magao.backend.module.judge.judge.JudgeFactory;
import com.magao.backend.module.practice.entity.PracticeQuestion;
import com.magao.backend.module.practice.entity.PracticeRecord;
import com.magao.backend.module.practice.mapper.PracticeQuestionMapper;
import com.magao.backend.module.practice.mapper.PracticeRecordMapper;
import com.magao.backend.module.practice.service.PracticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 练习服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeServiceImpl extends ServiceImpl<PracticeQuestionMapper, PracticeQuestion> implements PracticeService {

    private final PracticeRecordMapper practiceRecordMapper;
    private final JudgeFactory judgeFactory;

    @Override
    public Page<PracticeQuestion> pageList(PageDTO pageDTO, Integer language, Integer difficulty) {
        Page<PracticeQuestion> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<PracticeQuestion> wrapper = new LambdaQueryWrapper<>();
        if (language != null) { wrapper.eq(PracticeQuestion::getLanguage, language); }
        if (difficulty != null) { wrapper.eq(PracticeQuestion::getDifficulty, difficulty); }
        wrapper.orderByDesc(PracticeQuestion::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public PracticeQuestion getDetail(Long questionId) {
        PracticeQuestion question = getById(questionId);
        if (question == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        return question;
    }

    @Override
    public PracticeRecord submit(Long userId, Long questionId, String code, Integer language) {
        PracticeQuestion question = getById(questionId);
        if (question == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }

        // 创建练习记录
        PracticeRecord record = new PracticeRecord();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setCode(code);
        record.setLanguage(language);
        record.setJudgeStatus(0);
        practiceRecordMapper.insert(record);

        log.info("练习提交：userId={}, questionId={}, recordId={}", userId, questionId, record.getId());

        // 构建判题提交参数
        JudgeSubmitDTO submitDTO = new JudgeSubmitDTO();
        submitDTO.setQuestionId(questionId);
        submitDTO.setCode(code);
        // 根据language整数映射到语言标识
        submitDTO.setLang(convertLanguage(language));

        try {
            // 调用对应语言的判题实现进行评测
            JudgeResultDTO result = judgeFactory.getJudge(submitDTO.getLang()).judge(submitDTO);

            // 更新判题结果到记录
            record.setJudgeStatus(convertJudgeStatus(result.getStatus()));
            record.setExecuteTime(result.getTimeUsed() != null ? result.getTimeUsed().intValue() : null);
            record.setMemoryUsage(result.getMemoryUsed() != null ? result.getMemoryUsed().intValue() : null);
            record.setPassCaseCount(result.getPassedCases());
            record.setTotalCaseCount(result.getTotalCases());
            practiceRecordMapper.updateById(record);

            log.info("判题完成：recordId={}, status={}", record.getId(), result.getStatus());
        } catch (Exception e) {
            log.error("判题异常：recordId={}", record.getId(), e);
            record.setJudgeStatus(8); // SYSTEM_ERROR
            practiceRecordMapper.updateById(record);
        }

        return record;
    }

    /**
     * 将语言整数编码转换为语言标识字符串
     */
    private String convertLanguage(Integer language) {
        if (language == null) { return "python"; }
        return switch (language) {
            case 1 -> "python";
            case 2 -> "cpp";
            case 3 -> "goc";
            case 4 -> "scratch3";
            default -> "python";
        };
    }

    /**
     * 将判题状态字符串转换为整数编码
     */
    private Integer convertJudgeStatus(String status) {
        if (status == null) { return 8; }
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
