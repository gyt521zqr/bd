package com.magao.backend.module.judge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 草稿保存 DTO
 *
 * @author Magao开发团队
 */
@Data
@Schema(description = "草稿保存参数")
public class DraftDTO {

    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID")
    private Long questionId;

    @Schema(description = "编程语言")
    private String lang;

    @Schema(description = "代码文本")
    private String code;

    @Schema(description = "Scratch项目JSON")
    private String scratchJson;
}
