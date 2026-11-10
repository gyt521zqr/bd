package com.magao.backend.module.file.controller;

import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.file.entity.FileRecord;
import com.magao.backend.module.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "文件管理", description = "文件上传、下载、删除接口")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController extends BaseController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileRecord> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(required = false, defaultValue = "default") String bizType,
                                    @RequestParam(required = false) Long bizId) {
        return Result.success(fileService.upload(file, bizType, bizId));
    }

    @Operation(summary = "获取文件下载URL")
    @GetMapping("/download")
    public Result<String> download(@RequestParam Long fileId) {
        return Result.success(fileService.getDownloadUrl(fileId));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.deleteFile(id);
        return Result.success();
    }
}
