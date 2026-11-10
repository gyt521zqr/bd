package com.magao.backend.module.practice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.practice.entity.ScratchWork;
import com.magao.backend.module.practice.service.ScratchWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Scratch 作品管理控制器
 * <p>
 * 提供 Scratch 自由创作作品的保存、加载、删除、导出等API接口
 * </p>
 *
 * @author Magao开发团队
 */
@Tag(name = "Scratch作品管理", description = "Scratch自由创作作品保存、加载、删除、导出等接口")
@RestController
@RequestMapping("/api/scratch/work")
@RequiredArgsConstructor
public class ScratchWorkController extends BaseController {

    private final ScratchWorkService scratchWorkService;

    @Operation(summary = "保存Scratch作品")
    @PostMapping("/save")
    public Result<ScratchWork> save(@RequestParam String title,
                                     @RequestParam(required = false) String description,
                                     @RequestParam("sb3File") MultipartFile sb3File,
                                     @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                                     @RequestParam(required = false, defaultValue = "0") Integer isPublic) {
        return Result.success(scratchWorkService.saveWork(
                getCurrentUserId(), title, description, sb3File, thumbnail, isPublic));
    }

    @Operation(summary = "作品列表")
    @GetMapping("/list")
    public Result<Page<ScratchWork>> list(PageDTO pageDTO) {
        return Result.success(scratchWorkService.loadWorks(getCurrentUserId(), pageDTO.getPageNum(), pageDTO.getPageSize()));
    }

    @Operation(summary = "作品详情")
    @GetMapping("/{id}")
    public Result<ScratchWork> detail(@PathVariable Long id) {
        return Result.success(scratchWorkService.getWorkDetail(id));
    }

    @Operation(summary = "加载作品（下载sb3文件）")
    @GetMapping("/{id}/download")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        try (InputStream inputStream = scratchWorkService.loadWork(id, getCurrentUserId());
             OutputStream outputStream = response.getOutputStream()) {

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"project_" + id + ".sb3\"");

            // 流式传输
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("下载作品失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除作品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scratchWorkService.deleteWork(id, getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "导出作品（获取临时下载链接）")
    @GetMapping("/{id}/export")
    public Result<String> exportWork(@PathVariable Long id) {
        return Result.success(scratchWorkService.exportWork(id));
    }
}
