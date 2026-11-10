package com.magao.backend.module.judge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 统一判题提交 DTO
 * <p>
 * 兼容代码文本（Python/C++/GoC）与 Scratch 积木 JSON
 * 通过 lang 字段分发对应语言判题逻辑
 * </p>
 *
 * @author Magao开发团队
 */
@Data
@Schema(description = "判题提交参数")
public class JudgeSubmitDTO {

    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID")
    private Long questionId;

    @NotBlank(message = "编程语言不能为空")
    @Schema(description = "编程语言：scratch3/python/cpp/goc")
    private String lang;

    @Schema(description = "代码文本（Python/C++/GoC）")
    private String code;

    @Schema(description = "Scratch项目JSON")
    private String scratchJson;

    @Schema(description = "Scratch素材文件URL列表")
    private String scratchAssets;

    @Schema(description = "Scratch作品封面缩略图（前端生成的 base64 dataURL）")
    private String scratchCover;

    @Schema(description = "考试ID（考试模式）")
    private Long examId;

    @Schema(description = "是否考试模式")
    private Boolean isExam = false;
}
