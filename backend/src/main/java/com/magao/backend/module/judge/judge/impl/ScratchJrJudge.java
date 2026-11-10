package com.magao.backend.module.judge.judge.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magao.backend.module.judge.dto.JudgeResultDTO;
import com.magao.backend.module.judge.dto.JudgeSubmitDTO;
import com.magao.backend.module.judge.dto.RunResultDTO;
import com.magao.backend.module.judge.judge.AbstractJudge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * ScratchJr judge implementation.
 * <p>
 * ScratchJr 项目在浏览器端（Blockly + Canvas）运行，后端做结构化校验：
 * 解析项目 JSON，确认包含有效脚本（积木/页面），据此判定项目是否为有效提交。
 * 图形化题目的标准判分模型通常为"积木/结构检查"或"人工/半自动评审"，
 * 暂不以标准 OJ 比对方式判分。
 * </p>
 *
 * @author Magao Dev Team
 */
@Slf4j
@Component
public class ScratchJrJudge extends AbstractJudge {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getLang() {
        return "scratchjr";
    }

    @Override
    public int getJudge0LanguageId() {
        return 71;
    }

    @Override
    public JudgeResultDTO judge(JudgeSubmitDTO submitDTO) {
        log.info("ScratchJr judge: questionId={}", submitDTO.getQuestionId());

        String projectJson = submitDTO.getScratchJson();
        if (projectJson == null || projectJson.isEmpty()) {
            if (submitDTO.getCode() == null || submitDTO.getCode().isEmpty()) {
                return JudgeResultDTO.builder()
                        .status("COMPILE_ERROR").score(0).totalScore(100)
                        .passedCases(0).totalCases(1)
                        .errorMessage("ScratchJr 项目数据为空").build();
            }
            projectJson = submitDTO.getCode();
        }

        try {
            JsonNode root = MAPPER.readTree(projectJson);
            if (!hasValidScripts(root)) {
                return JudgeResultDTO.builder()
                        .status("COMPILE_ERROR").score(0).totalScore(100)
                        .passedCases(0).totalCases(1)
                        .errorMessage("ScratchJr 项目中未找到有效脚本").build();
            }
            return JudgeResultDTO.builder()
                    .status("ACCEPTED").score(100).totalScore(100)
                    .passedCases(1).totalCases(1)
                    .output("ScratchJr 项目结构校验通过（运行验证在浏览器端完成）").build();
        } catch (Exception e) {
            log.error("ScratchJr judge error", e);
            return JudgeResultDTO.builder()
                    .status("SYSTEM_ERROR").score(0).totalScore(100)
                    .passedCases(0).totalCases(1)
                    .errorMessage("ScratchJr 判题系统错误: " + e.getMessage()).build();
        }
    }

    @Override
    public RunResultDTO run(JudgeSubmitDTO submitDTO) {
        return RunResultDTO.builder()
                .success(true)
                .output("ScratchJr 项目在浏览器端运行（Blockly 运行时）").build();
    }

    @Override
    public String validate(JudgeSubmitDTO submitDTO) {
        String baseError = super.validate(submitDTO);
        if (baseError != null) return baseError;

        boolean hasData = (submitDTO.getScratchJson() != null && !submitDTO.getScratchJson().isEmpty())
                || (submitDTO.getCode() != null && !submitDTO.getCode().isEmpty());
        if (!hasData) {
            return "ScratchJr 项目数据不能为空";
        }
        return null;
    }

    /**
     * 检查 ScratchJr 项目 JSON 是否包含有效内容（存在非空子节点即视为有脚本）
     */
    private boolean hasValidScripts(JsonNode root) {
        if (root == null || root.isNull()) return false;
        if (root.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = root.fields();
            while (it.hasNext()) {
                JsonNode v = it.next().getValue();
                if (v != null && !v.isNull() && !v.isTextual() && !v.isNumber()) {
                    return true;
                }
            }
        }
        return root.size() > 0;
    }
}
