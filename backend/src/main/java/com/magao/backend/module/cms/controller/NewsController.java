package com.magao.backend.module.cms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.cms.entity.News;
import com.magao.backend.module.cms.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资讯管理控制器
 */
@Tag(name = "资讯管理", description = "资讯查询、创建、管理等接口")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController extends BaseController {

    private final NewsService newsService;

    @Operation(summary = "资讯列表（公开）")
    @GetMapping
    public Result<Page<News>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setPageNum(page);
        pageDTO.setPageSize(size);
        // 公开接口默认只返回已发布的
        if (status == null) {
            status = 1;
        }
        return Result.success(newsService.pageList(pageDTO, categoryId, status));
    }

    @Operation(summary = "资讯详情（公开）")
    @GetMapping("/{id}")
    public Result<News> detail(@PathVariable Long id) {
        newsService.incrementViewCount(id);
        return Result.success(newsService.getDetail(id));
    }

    @Operation(summary = "创建资讯")
    @PostMapping
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "资讯管理", type = "新增", description = "创建资讯")
    public Result<News> create(@RequestBody News news) {
        newsService.save(news);
        return Result.success(news);
    }

    @Operation(summary = "更新资讯")
    @PutMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "资讯管理", type = "修改", description = "更新资讯")
    public Result<News> update(@PathVariable Long id, @RequestBody News news) {
        news.setId(id);
        newsService.updateById(news);
        return Result.success(news);
    }

    @Operation(summary = "删除资讯")
    @DeleteMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "资讯管理", type = "删除", description = "删除资讯")
    public Result<Void> delete(@PathVariable Long id) {
        newsService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "置顶/取消置顶")
    @PutMapping("/{id}/pin")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "资讯管理", type = "修改", description = "置顶/取消置顶资讯")
    public Result<Void> togglePin(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        News news = new News();
        news.setId(id);
        news.setIsPinned((Integer) params.get("isPinned"));
        newsService.updateById(news);
        return Result.success();
    }

    @Operation(summary = "发布/下架资讯")
    @PutMapping("/{id}/status")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "资讯管理", type = "修改", description = "发布/下架资讯")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        News news = new News();
        news.setId(id);
        Object status = params.get("status");
        if (status instanceof String) {
            news.setStatus("published".equals(status) ? 1 : ("draft".equals(status) ? 0 : 2));
        } else {
            news.setStatus((Integer) status);
        }
        newsService.updateById(news);
        return Result.success();
    }
}
