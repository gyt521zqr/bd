package com.magao.backend.module.judge.judge.impl;

import com.magao.backend.common.enums.LanguageEnum;
import com.magao.backend.module.judge.dto.JudgeResultDTO;
import com.magao.backend.module.judge.dto.JudgeSubmitDTO;
import com.magao.backend.module.judge.dto.RunResultDTO;
import com.magao.backend.module.judge.judge.AbstractJudge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Python Turtle judge implementation.
 * <p>
 * 通过 Judge0（language_id=71）真实运行 Python 代码。
 * 注意：Judge0 默认运行环境为 headless（无图形显示），turtle 图形绘制类代码会运行失败；
 * 若题目考察非图形逻辑（变量、运算、循环等），可正常判分。纯图形题目建议在浏览器端
 * Pyodide（turtle 模式）验证。
 * </p>
 *
 * @author Magao Dev Team
 */
@Slf4j
@Component
public class PythonTurtleJudge extends AbstractJudge {

    @Override
    public String getLang() {
        return "pythonturtle";
    }

    @Override
    public int getJudge0LanguageId() {
        return LanguageEnum.PYTHON.getJudge0LanguageId();
    }

    @Override
    public JudgeResultDTO judge(JudgeSubmitDTO submitDTO) {
        log.info("Python Turtle judge: questionId={}", submitDTO.getQuestionId());

        if (submitDTO.getCode() == null || submitDTO.getCode().trim().isEmpty()) {
            return JudgeResultDTO.builder()
                    .status("COMPILE_ERROR").score(0).totalScore(100)
                    .passedCases(0).totalCases(1).errorMessage("代码为空").build();
        }

        try {
            return doJudge(submitDTO.getCode(), submitDTO.getQuestionId(), getJudge0LanguageId());
        } catch (Exception e) {
            log.error("Python Turtle judge error", e);
            return JudgeResultDTO.builder()
                    .status("SYSTEM_ERROR").score(0).totalScore(100)
                    .passedCases(0).totalCases(0)
                    .errorMessage("判题系统错误: " + e.getMessage()).build();
        }
    }

    @Override
    public RunResultDTO run(JudgeSubmitDTO submitDTO) {
        log.info("Python Turtle run: questionId={}", submitDTO.getQuestionId());
        try {
            return doRun(submitDTO.getCode(), getJudge0LanguageId(), submitDTO.getQuestionId(), "");
        } catch (Exception e) {
            log.error("Python Turtle run error", e);
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
