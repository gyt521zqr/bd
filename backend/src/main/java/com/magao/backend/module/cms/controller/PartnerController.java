package com.magao.backend.module.cms.controller;

import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.cms.entity.Partner;
import com.magao.backend.module.cms.service.PartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 合作伙伴管理控制器
 */
@Tag(name = "合作伙伴管理", description = "合作伙伴查询、创建、管理等接口")
@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
public class PartnerController extends BaseController {

    private final PartnerService partnerService;

    @Operation(summary = "合作伙伴列表（公开）")
    @GetMapping
    public Result<List<Partner>> list() {
        return Result.success(partnerService.listAll());
    }

    @Operation(summary = "创建合作伙伴")
    @PostMapping
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "合作伙伴管理", type = "新增", description = "创建合作伙伴")
    public Result<Partner> create(@RequestBody Partner partner) {
        partnerService.save(partner);
        return Result.success(partner);
    }

    @Operation(summary = "更新合作伙伴")
    @PutMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "合作伙伴管理", type = "修改", description = "更新合作伙伴")
    public Result<Partner> update(@PathVariable Long id, @RequestBody Partner partner) {
        partner.setId(id);
        partnerService.updateById(partner);
        return Result.success(partner);
    }

    @Operation(summary = "删除合作伙伴")
    @DeleteMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "合作伙伴管理", type = "删除", description = "删除合作伙伴")
    public Result<Void> delete(@PathVariable Long id) {
        partnerService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "更新排序")
    @PutMapping("/{id}/sort")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "合作伙伴管理", type = "修改", description = "更新合作伙伴排序")
    public Result<Void> updateSort(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Partner partner = new Partner();
        partner.setId(id);
        partner.setSortOrder((Integer) params.get("sortOrder"));
        partnerService.updateById(partner);
        return Result.success();
    }
}
