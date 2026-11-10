package com.magao.backend.module.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 竞赛提交作品 DTO
 *
 * @author Magao开发团队
 */
@Data
@Schema(description = "竞赛提交作品参数")
public class CompetitionSubmitDTO {

    @NotNull(message = "竞赛ID不能为空")
    @Schema(description = "竞赛ID")
    private Long competitionId;

    @NotBlank(message = "Scratch sb3 数据不能为空")
    @Schema(description = "Scratch sb3 数据（JSON）")
    private String sb3Data;
}
