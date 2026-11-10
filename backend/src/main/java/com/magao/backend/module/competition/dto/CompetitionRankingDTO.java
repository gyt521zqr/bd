package com.magao.backend.module.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 竞赛排行榜 DTO
 *
 * @author Magao开发团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "竞赛排行榜项")
public class CompetitionRankingDTO {

    @Schema(description = "排名")
    private Integer ranking;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "提交时间")
    private String submitTime;
}
