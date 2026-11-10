package com.magao.backend.module.judge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 判题结果 DTO
 * <p>
 * 统一返回结构，兼容所有语言判题结果
 * </p>
 *
 * @author Magao开发团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "判题结果")
public class JudgeResultDTO {

    @Schema(description = "提交记录ID")
    private Long recordId;

    @Schema(description = "判题状态：PENDING/RUNNING/ACCEPTED/WRONG_ANSWER/TIME_LIMIT_EXCEEDED/MEMORY_LIMIT_EXCEEDED/RUNTIME_ERROR/COMPILE_ERROR/SYSTEM_ERROR")
    private String status;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "总分")
    private Integer totalScore;

    @Schema(description = "通过用例数")
    private Integer passedCases;

    @Schema(description = "总用例数")
    private Integer totalCases;

    @Schema(description = "执行时间（ms）")
    private Long timeUsed;

    @Schema(description = "内存消耗（KB）")
    private Long memoryUsed;

    @Schema(description = "运行输出")
    private String output;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "Scratch作品封面缩略图URL（可选）")
    private String coverUrl;

    @Schema(description = "各用例判题详情")
    private List<CaseResultDTO> caseResults;

    /**
     * 单个用例判题结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单个用例判题结果")
    public static class CaseResultDTO {

        @Schema(description = "用例ID")
        private Long caseId;

        @Schema(description = "是否通过")
        private Boolean passed;

        @Schema(description = "实际输出")
        private String actualOutput;

        @Schema(description = "期望输出")
        private String expectedOutput;

        @Schema(description = "执行时间（ms）")
        private Long timeUsed;

        @Schema(description = "内存消耗（KB）")
        private Long memoryUsed;

        @Schema(description = "错误信息")
        private String errorMessage;
    }
}
