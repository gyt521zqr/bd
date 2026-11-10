package com.magao.backend.module.cms.controller;

import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.cms.entity.Category;
import com.magao.backend.module.cms.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分类管理控制器
 */
@Tag(name = "分类管理", description = "分类查询、创建、管理等接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController extends BaseController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表（公开）")
    @GetMapping
    public Result<List<Category>> list(@RequestParam(required = false) String type) {
        return Result.success(categoryService.listByType(type));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "分类管理", type = "新增", description = "创建分类")
    public Result<Category> create(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success(category);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "分类管理", type = "修改", description = "更新分类")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.success(category);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "分类管理", type = "删除", description = "删除分类")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "更新分类排序")
    @PutMapping("/{id}/sort")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "分类管理", type = "修改", description = "更新分类排序")
    public Result<Void> updateSort(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Category category = new Category();
        category.setId(id);
        category.setSortOrder((Integer) params.get("sortOrder"));
        categoryService.updateById(category);
        return Result.success();
    }
}
