package com.magao.backend.module.growth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.growth.service.GrowthService;
import com.magao.backend.module.growth.vo.LeaderboardItemVO;
import com.magao.backend.module.growth.vo.LevelInfoVO;
import com.magao.backend.module.growth.vo.MedalItemVO;
import com.magao.backend.module.growth.vo.PointRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 成长体系控制器（对齐前端 /growth/* 契约）
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "成长体系", description = "积分、勋章、等级、排行榜、周报等接口")
@RestController
@RequestMapping("/api/growth")
@RequiredArgsConstructor
public class GrowthController extends BaseController {

    private final GrowthService growthService;

    @Operation(summary = "积分概览")
    @GetMapping("/points")
    public Result<Map<String, Object>> points() {
        return Result.success(growthService.getPointsSummary(getCurrentUserId()));
    }

    @Operation(summary = "积分记录")
    @GetMapping("/points/records")
    public Result<Page<PointRecordVO>> pointRecords(PageDTO pageDTO,
                                                     @RequestParam(required = false) String type) {
        return Result.success(growthService.getPointRecords(pageDTO, getCurrentUserId(), type));
    }

    @Operation(summary = "勋章列表")
    @GetMapping("/medals")
    public Result<List<MedalItemVO>> medals() {
        return Result.success(growthService.getMedals(getCurrentUserId()));
    }

    @Operation(summary = "等级信息")
    @GetMapping("/level")
    public Result<LevelInfoVO> level() {
        return Result.success(growthService.getLevelInfo(getCurrentUserId()));
    }

    @Operation(summary = "排行榜")
    @GetMapping("/leaderboard")
    public Result<List<LeaderboardItemVO>> leaderboard(@RequestParam(defaultValue = "points") String type,
                                                       @RequestParam(defaultValue = "week") String period) {
        return Result.success(growthService.getLeaderboard(type, period));
    }

    @Operation(summary = "学习周报")
    @GetMapping("/weekly-report")
    public Result<Map<String, Object>> weeklyReport() {
        return Result.success(growthService.getWeeklyReport(getCurrentUserId()));
    }
}
