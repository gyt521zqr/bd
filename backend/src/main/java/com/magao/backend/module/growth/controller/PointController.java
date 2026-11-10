package com.magao.backend.module.growth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.growth.entity.PointLog;
import com.magao.backend.module.growth.entity.UserPoint;
import com.magao.backend.module.growth.service.GrowthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 积分管理控制器
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "积分管理", description = "积分查询、日志等接口")
@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController extends BaseController {

    private final GrowthService growthService;

    @Operation(summary = "积分信息")
    @GetMapping("/info")
    public Result<UserPoint> info() {
        return Result.success(growthService.getPointInfo(getCurrentUserId()));
    }

    @Operation(summary = "积分日志")
    @GetMapping("/log")
    public Result<Page<PointLog>> log(PageDTO pageDTO) {
        return Result.success(growthService.getPointLog(pageDTO, getCurrentUserId()));
    }
}
