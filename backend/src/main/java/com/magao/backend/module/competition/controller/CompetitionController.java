package com.magao.backend.module.competition.controller;

import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.result.Result;
import com.magao.backend.common.util.IpUtil;
import com.magao.backend.module.competition.dto.CompetitionCreateDTO;
import com.magao.backend.module.competition.dto.CompetitionRankingDTO;
import com.magao.backend.module.competition.dto.CompetitionSubmitDTO;
import com.magao.backend.module.competition.entity.CompetitionInfo;
import com.magao.backend.module.competition.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 竞赛管理控制器
 * <p>
 * 提供竞赛创建、加入、提交、排行榜等API接口
 * </p>
 *
 * @author Magao开发团队
 */
@Tag(name = "竞赛管理", description = "竞赛创建、加入、提交、排行榜等接口")
@RestController
@RequestMapping("/api/competition")
@RequiredArgsConstructor
public class CompetitionController extends BaseController {

    private final CompetitionService competitionService;

    @Operation(summary = "创建竞赛")
    @PostMapping("/create")
    public Result<CompetitionInfo> create(@Valid @RequestBody CompetitionCreateDTO createDTO) {
        return Result.success(competitionService.createCompetition(createDTO, getCurrentUserId()));
    }

    @Operation(summary = "竞赛列表")
    @GetMapping("/list")
    public Result<List<CompetitionInfo>> list(PageDTO pageDTO,
                                               @RequestParam(required = false) Integer status) {
        return Result.success(competitionService.listCompetitions(pageDTO.getPageNum(), pageDTO.getPageSize(), status));
    }

    @Operation(summary = "竞赛详情")
    @GetMapping("/{id}")
    public Result<CompetitionInfo> detail(@PathVariable Long id) {
        return Result.success(competitionService.getCompetitionDetail(id));
    }

    @Operation(summary = "竞赛详情（段序别名 /detail/{id}）")
    @GetMapping("/detail/{id}")
    public Result<CompetitionInfo> detailAlias(@PathVariable Long id) {
        return Result.success(competitionService.getCompetitionDetail(id));
    }

    @Operation(summary = "加入竞赛")
    @PostMapping("/{id}/join")
    public Result<Void> join(@PathVariable Long id) {
        competitionService.joinCompetition(id, getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "提交作品（带竞赛ID段）")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id, @Valid @RequestBody CompetitionSubmitDTO submitDTO,
                               HttpServletRequest request) {
        submitDTO.setCompetitionId(id);
        competitionService.submitWork(submitDTO, getCurrentUserId(), IpUtil.getClientIp(request));
        return Result.success();
    }

    @Operation(summary = "提交作品（无ID段，competitionId 取自 body，对齐前端 submitCompetitionWork）")
    @PostMapping("/submit")
    public Result<Void> submitNoId(@Valid @RequestBody CompetitionSubmitDTO submitDTO,
                                   HttpServletRequest request) {
        if (submitDTO.getCompetitionId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "竞赛ID不能为空");
        }
        competitionService.submitWork(submitDTO, getCurrentUserId(), IpUtil.getClientIp(request));
        return Result.success();
    }

    @Operation(summary = "获取排行榜")
    @GetMapping("/{id}/ranking")
    public Result<List<CompetitionRankingDTO>> ranking(@PathVariable Long id) {
        return Result.success(competitionService.getRanking(id));
    }

    @Operation(summary = "获取竞赛成绩/结果（对齐前端 getCompetitionResults）")
    @GetMapping("/{id}/results")
    public Result<List<Map<String, Object>>> results(@PathVariable Long id) {
        List<CompetitionRankingDTO> ranking = competitionService.getRanking(id);
        List<Map<String, Object>> result = new ArrayList<>();
        for (CompetitionRankingDTO r : ranking) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", r.getRanking());
            m.put("userId", r.getUserId());
            m.put("nickname", r.getUsername());
            m.put("avatar", r.getAvatar());
            m.put("score", r.getScore());
            m.put("submitTime", r.getSubmitTime());
            m.put("timeUsed", 0);
            result.add(m);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取竞赛状态")
    @GetMapping("/{id}/status")
    public Result<CompetitionInfo> status(@PathVariable Long id) {
        return Result.success(competitionService.getCompetitionStatus(id));
    }
}
