package com.magao.backend.module.cms.controller;

import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.cms.entity.Banner;
import com.magao.backend.module.cms.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Banner管理控制器
 */
@Tag(name = "Banner管理", description = "Banner查询、创建、管理等接口")
@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
public class BannerController extends BaseController {

    private final BannerService bannerService;

    @Operation(summary = "Banner列表（公开）")
    @GetMapping
    public Result<List<Banner>> list(@RequestParam(required = false, defaultValue = "home") String position) {
        return Result.success(bannerService.listByPosition(position));
    }

    @Operation(summary = "创建Banner")
    @PostMapping
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "Banner管理", type = "新增", description = "创建Banner")
    public Result<Banner> create(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success(banner);
    }

    @Operation(summary = "更新Banner")
    @PutMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "Banner管理", type = "修改", description = "更新Banner")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.updateById(banner);
        return Result.success(banner);
    }

    @Operation(summary = "删除Banner")
    @DeleteMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "Banner管理", type = "删除", description = "删除Banner")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success();
    }
}
