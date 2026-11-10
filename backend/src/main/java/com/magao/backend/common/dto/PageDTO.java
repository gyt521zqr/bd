package com.magao.backend.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询数据传输对象
 * <p>
 * 用于接收分页查询的通用参数：
 * - pageNum：页码（从1开始）
 * - pageSize：每页大小
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class PageDTO {

    /** 页码（从1开始，默认第1页） */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /** 每页大小（默认10条，最大100条） */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 10;
}
