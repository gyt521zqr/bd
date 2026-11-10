package com.magao.backend.module.cms.controller;

import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.system.service.SystemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页配置控制器
 */
@Slf4j
@Tag(name = "首页配置", description = "首页配置查询、更新等接口")
@RestController
@RequestMapping("/api/homepage")
@RequiredArgsConstructor
public class HomepageController extends BaseController {

    private final SystemService systemService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取首页配置（公开）")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        String value = systemService.getConfigValue("homepage_config");
        Map<String, Object> config;
        if (value != null && !value.isEmpty()) {
            try {
                config = objectMapper.readValue(value, Map.class);
            } catch (JsonProcessingException e) {
                log.error("解析首页配置失败", e);
                config = getDefaultConfig();
            }
        } else {
            config = getDefaultConfig();
        }
        return Result.success(config);
    }

    @Operation(summary = "更新首页配置")
    @PutMapping("/config")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "首页配置", type = "修改", description = "更新首页配置")
    public Result<Map<String, Object>> updateConfig(@RequestBody Map<String, Object> config) {
        try {
            String value = objectMapper.writeValueAsString(config);
            systemService.setConfigValue("homepage_config", value);
            return Result.success(config);
        } catch (JsonProcessingException e) {
            log.error("保存首页配置失败", e);
            return Result.fail("保存失败");
        }
    }

    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", "100000+");
        stats.put("activeStudents", "50000+");
        stats.put("coveredCities", "100+");
        stats.put("partnerCount", "50+");
        stats.put("workCount", "10000+");
        config.put("stats", stats);
        config.put("entries", new ArrayList<>());
        config.put("media", new ArrayList<>());
        config.put("bannerAutoPlay", true);
        config.put("bannerInterval", 4000);
        config.put("showCourseCategory", true);
        config.put("showNews", true);
        config.put("showPartner", true);
        return config;
    }
}
