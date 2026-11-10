package com.magao.backend.module.system.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.system.entity.SysNotice;
import com.magao.backend.module.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器
 * <p>
 * 提供系统管理相关的API接口：系统配置、公告管理
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "系统管理", description = "系统配置、公告等接口")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController extends BaseController {

    private final SystemService systemService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取配置值")
    @GetMapping("/config/{key}")
    public Result<String> getConfigValue(@PathVariable("key") String configKey) {
        return Result.success(systemService.getConfigValue(configKey));
    }

    @Operation(summary = "设置配置值")
    @PutMapping("/config")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "系统管理", type = "修改", description = "设置系统配置")
    public Result<Void> setConfigValue(@RequestParam String configKey,
                                      @RequestParam String configValue) {
        systemService.setConfigValue(configKey, configValue);
        return Result.success();
    }

    @Operation(summary = "系统公告列表")
    @GetMapping("/notices")
    public Result<List<SysNotice>> listNotices() {
        return Result.success(systemService.listNotices());
    }

    @Operation(summary = "公开站点配置（站名/Logo/ICP/客服/导航/功能开关）")
    @GetMapping("/site-config")
    public Result<Map<String, Object>> getSiteConfig() {
        Map<String, Object> cfg = new java.util.HashMap<>();
        cfg.put("siteName", defaultIfNull(systemService.getConfigValue("site_name"), "码高教育"));
        cfg.put("siteLogo", systemService.getConfigValue("site_logo"));
        cfg.put("icp", systemService.getConfigValue("icp"));
        cfg.put("contactPhone", systemService.getConfigValue("contact_phone"));
        cfg.put("contactEmail", systemService.getConfigValue("contact_email"));
        cfg.put("contactAddress", systemService.getConfigValue("contact_address"));
        cfg.put("copyright", defaultIfNull(systemService.getConfigValue("copyright"), "Copyright 2024-2026 码高少儿编程教育平台"));

        String navRaw = systemService.getConfigValue("nav_menu");
        if (navRaw != null && !navRaw.trim().isEmpty()) {
            try {
                cfg.put("navMenu", objectMapper.readValue(navRaw, List.class));
            } catch (JsonProcessingException e) {
                log.warn("解析导航菜单配置失败", e);
                cfg.put("navMenu", Collections.emptyList());
            }
        } else {
            cfg.put("navMenu", Collections.emptyList());
        }

        cfg.put("registerEnabled", parseBool(systemService.getConfigValue("register_enabled"), true));
        cfg.put("maintenanceMode", parseBool(systemService.getConfigValue("maintenance_mode"), false));
        return Result.success(cfg);
    }

    private String defaultIfNull(String v, String d) {
        return v == null ? d : v;
    }

    private boolean parseBool(String v, boolean d) {
        if (v == null) return d;
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }
}
