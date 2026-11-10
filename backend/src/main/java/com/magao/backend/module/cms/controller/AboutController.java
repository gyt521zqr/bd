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
 * 关于我们配置控制器
 */
@Slf4j
@Tag(name = "关于我们配置", description = "关于我们配置查询、更新等接口")
@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutController extends BaseController {

    private final SystemService systemService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取关于我们配置（公开）")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        String value = systemService.getConfigValue("about_config");
        Map<String, Object> config;
        if (value != null && !value.isEmpty()) {
            try {
                config = objectMapper.readValue(value, Map.class);
            } catch (JsonProcessingException e) {
                log.error("解析关于我们配置失败", e);
                config = getDefaultConfig();
            }
        } else {
            config = getDefaultConfig();
        }
        return Result.success(config);
    }

    @Operation(summary = "更新关于我们配置")
    @PutMapping("/config")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "关于我们配置", type = "修改", description = "更新关于我们配置")
    public Result<Map<String, Object>> updateConfig(@RequestBody Map<String, Object> config) {
        try {
            String value = objectMapper.writeValueAsString(config);
            systemService.setConfigValue("about_config", value);
            return Result.success(config);
        } catch (JsonProcessingException e) {
            log.error("保存关于我们配置失败", e);
            return Result.fail("保存失败");
        }
    }

    /**
     * 未配置 about_config 时返回中性的空结构。
     * 不在此处编造成立年份、办公地址、客服电话/邮箱等事实性信息：
     * 联系方式统一由 GET /api/system/site-config 下发，避免两处配置互相矛盾；
     * 简介文案由前台默认值与后台「关于我们配置」提供，后台未填即显示中性文案。
     */
    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("title", "");
        config.put("content", "");
        config.put("foundedYear", "");
        config.put("timeline", new ArrayList<>());
        config.put("team", new ArrayList<>());
        config.put("companyName", "");
        config.put("slogan", "");
        return config;
    }
}
