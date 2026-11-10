package com.magao.backend.module.judge.judge.impl;

import com.magao.backend.common.enums.LanguageEnum;
import com.magao.backend.module.judge.dto.JudgeResultDTO;
import com.magao.backend.module.judge.dto.JudgeSubmitDTO;
import com.magao.backend.module.judge.dto.RunResultDTO;
import com.magao.backend.module.judge.judge.AbstractJudge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Python judge implementation.
 * <p>
 * 通过 Judge0 沙箱真实编译运行 Python 代码（language_id=71），
 * 逐测试用例比对 expected_output 得出判分结果。
 * </p>
 *
 * @author Magao Dev Team
 */
@Slf4j
@Component
public class PythonJudge extends AbstractJudge {

    @Override
    public String getLang() {
        return "python";
    }

    @Override
    public int getJudge0LanguageId() {
        return LanguageEnum.PYTHON.getJudge0LanguageId();
    }

    @Override
    public JudgeResultDTO judge(JudgeSubmitDTO submitDTO) {
        log.info("Python judge: questionId={}", submitDTO.getQuestionId());

        if (submitDTO.getCode() == null || submitDTO.getCode().trim().isEmpty()) {
            return JudgeResultDTO.builder()
                    .status("COMPILE_ERROR").score(0).totalScore(100)
                    .passedCases(0).totalCases(1).errorMessage("代码为空").build();
        }

        try {
            return doJudge(submitDTO.getCode(), submitDTO.getQuestionId(), getJudge0LanguageId());
        } catch (Exception e) {
            log.error("Python judge error", e);
            return JudgeResultDTO.builder()
                    .status("SYSTEM_ERROR").score(0).totalScore(100)
                    .passedCases(0).totalCases(0)
                    .errorMessage("判题系统错误: " + e.getMessage()).build();
        }
    }

    @Override
    public RunResultDTO run(JudgeSubmitDTO submitDTO) {
        log.info("Python run: questionId={}", submitDTO.getQuestionId());
        try {
            return doRun(submitDTO.getCode(), getJudge0LanguageId(), submitDTO.getQuestionId(), "");
        } catch (Exception e) {
            log.error("Python run error", e);
            return RunResultDTO.builder().success(false).errorMessage("运行错误: " + e.getMessage()).build();
        }
    }

    @Override
    public String validate(JudgeSubmitDTO submitDTO) {
        String baseError = super.validate(submitDTO);
        if (baseError != null) return baseError;

        if (submitDTO.getCode() == null || submitDTO.getCode().trim().isEmpty()) {
            return "代码不能为空";
        }
        if (submitDTO.getCode().length() > 65535) {
            return "代码长度超过限制 (最大 65535 字符)";
        }
        return null;
    }
}
