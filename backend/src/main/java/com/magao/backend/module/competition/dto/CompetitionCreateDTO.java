package com.magao.backend.module.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建竞赛 DTO
 *
 * @author Magao开发团队
 */
@Data
@Schema(description = "创建竞赛参数")
public class CompetitionCreateDTO {

    @NotBlank(message = "竞赛标题不能为空")
    @Schema(description = "竞赛标题")
    private String title;

    @Schema(description = "竞赛描述")
    private String description;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "允许的积木分类（JSON数组，如 [\"motion\",\"control\",\"operator\"]）")
    private String allowedBlockCategories;

    @Schema(description = "最大参赛人数")
    private Integer maxParticipants;
}
