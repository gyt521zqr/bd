package com.magao.backend.module.judge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码运行结果 DTO（仅运行不判题）
 *
 * @author Magao开发团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "代码运行结果")
public class RunResultDTO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "运行输出")
    private String output;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "执行时间（ms）")
    private Long timeUsed;
}
