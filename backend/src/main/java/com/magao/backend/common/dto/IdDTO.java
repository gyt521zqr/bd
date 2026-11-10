package com.magao.backend.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ID参数数据传输对象
 * <p>
 * 用于接收只需要ID参数的请求（如删除、查询详情等）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class IdDTO {

    /** 主键ID */
    @NotNull(message = "ID不能为空")
    private Long id;
}
