package com.magao.backend.module.oss.controller;

import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.file.entity.FileRecord;
import com.magao.backend.module.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 对象存储控制器（OSS 单源，P2 补齐）
 * <p>
 * 复用既有 file 模块的 {@link FileService}（本地存储实现）作为底层存储，
 * 对外提供与前端 {@code src/api/oss.ts} 完全对齐的 7 个端点：
 * /upload /upload-token /signed-url /batch-signed-urls /file(DELETE) /batch-delete /list。
 * 另提供 /raw/{fileId} 公共读取端点，使上传后的文件 URL 真正可访问。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
@Tag(name = "对象存储(OSS)", description = "文件上传/删除/签名URL/列表，前端 src/api/oss.ts 对齐")
public class OssController extends BaseController {

    private final FileService fileService;

    /** 与 LocalFileServiceImpl.UPLOAD_DIR 保持一致 */
    private static final String UPLOAD_DIR = "C:\\tools\\magao-build\\uploads";

    private String rawUrl(Long fileId) {
        return "/api/oss/raw/" + fileId;
    }

    private Long parseFileId(String objectKey) {
        if (objectKey == null) return null;
        try {
            return Long.parseLong(objectKey.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Operation(summary = "文件上传（后端转发，小文件）")
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String id) {
        String bizType = type != null ? type : "default";
        Long bizId = null;
        if (id != null) {
            try { bizId = Long.parseLong(id.trim()); } catch (NumberFormatException ignored) {}
        }
        FileRecord record = fileService.upload(file, bizType, bizId);
        Map<String, Object> data = new HashMap<>(3);
        data.put("url", rawUrl(record.getId()));
        data.put("objectKey", String.valueOf(record.getId()));
        data.put("size", record.getFileSize() == null ? 0 : record.getFileSize());
        return Result.success(data);
    }

    @Operation(summary = "获取直传凭证（本地存储占位，返回可服务端上传的入口）")
    @GetMapping("/upload-token")
    public Result<Map<String, Object>> uploadToken(@RequestParam(required = false) String fileType,
                                                    @RequestParam(required = false) String dir) {
        Map<String, Object> token = new HashMap<>(7);
        token.put("accessKeyId", "local");
        token.put("accessKeySecret", "local");
        token.put("securityToken", "local");
        token.put("expiration", System.currentTimeMillis() + 3600_000L);
        token.put("bucket", "local");
        token.put("endpoint", "/api/oss/upload");
        token.put("dir", dir != null ? dir : "default");
        return Result.success(token);
    }

    @Operation(summary = "获取文件签名URL")
    @PostMapping("/signed-url")
    public Result<Map<String, Object>> signedUrl(@RequestBody Map<String, Object> body) {
        String objectKey = body.get("objectKey") != null ? body.get("objectKey").toString() : null;
        Object expObj = body.get("expires");
        long expires = expObj != null ? Long.parseLong(expObj.toString()) : 3600L;
        Long fid = parseFileId(objectKey);
        Map<String, Object> data = new HashMap<>(2);
        data.put("url", fid != null ? rawUrl(fid) : "");
        data.put("expiration", System.currentTimeMillis() + expires * 1000L);
        return Result.success(data);
    }

    @Operation(summary = "批量获取签名URL")
    @PostMapping("/batch-signed-urls")
    public Result<Map<String, Map<String, Object>>> batchSignedUrls(@RequestBody Map<String, Object> body) {
        Object keysObj = body.get("objectKeys");
        long expires = 3600L;
        if (body.get("expires") != null) {
            try { expires = Long.parseLong(body.get("expires").toString()); } catch (NumberFormatException ignored) {}
        }
        Map<String, Map<String, Object>> out = new HashMap<>();
        if (keysObj instanceof List) {
            for (Object k : (List<?>) keysObj) {
                String key = k != null ? k.toString() : "";
                Long fid = parseFileId(key);
                Map<String, Object> m = new HashMap<>(2);
                m.put("url", fid != null ? rawUrl(fid) : "");
                m.put("expiration", System.currentTimeMillis() + expires * 1000L);
                out.put(key, m);
            }
        }
        return Result.success(out);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/file")
    public Result<Void> deleteFile(@RequestBody Map<String, Object> body) {
        String objectKey = body.get("objectKey") != null ? body.get("objectKey").toString() : null;
        Long fid = parseFileId(objectKey);
        if (fid != null) {
            fileService.deleteFile(fid);
        }
        return Result.success();
    }

    @Operation(summary = "批量删除文件")
    @PostMapping("/batch-delete")
    public Result<Map<String, Object>> batchDelete(@RequestBody Map<String, Object> body) {
        Object keysObj = body.get("objectKeys");
        List<String> deleted = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        if (keysObj instanceof List) {
            for (Object k : (List<?>) keysObj) {
                String key = k != null ? k.toString() : "";
                Long fid = parseFileId(key);
                if (fid == null) { failed.add(key); continue; }
                try {
                    fileService.deleteFile(fid);
                    deleted.add(key);
                } catch (Exception e) {
                    failed.add(key);
                }
            }
        }
        Map<String, Object> data = new HashMap<>(2);
        data.put("deleted", deleted);
        data.put("failed", failed);
        return Result.success(data);
    }

    @Operation(summary = "获取文件列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) String prefix,
                                            @RequestParam(required = false) String marker,
                                            @RequestParam(required = false) Integer maxKeys) {
        List<FileRecord> all = fileService.list();
        List<Map<String, Object>> files = new ArrayList<>();
        int limit = maxKeys != null && maxKeys > 0 ? maxKeys : all.size();
        int count = 0;
        for (FileRecord r : all) {
            if (prefix != null && (r.getBizType() == null || !r.getBizType().startsWith(prefix))) {
                continue;
            }
            Map<String, Object> m = new HashMap<>(4);
            m.put("key", String.valueOf(r.getId()));
            m.put("size", r.getFileSize() == null ? 0 : r.getFileSize());
            m.put("lastModified", r.getUpdateTime() != null ? r.getUpdateTime().toString() : "");
            m.put("url", rawUrl(r.getId()));
            files.add(m);
            if (++count >= limit) break;
        }
        Map<String, Object> data = new HashMap<>(3);
        data.put("files", files);
        data.put("nextMarker", null);
        data.put("isTruncated", false);
        return Result.success(data);
    }

    @Operation(summary = "公共读取文件（供上传后回显/下载，免登录，兼容本地与MinIO存储）")
    @GetMapping("/raw/{fileId}")
    public void raw(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        FileRecord record = fileService.getById(fileId);
        if (record == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (InputStream is = fileService.download(fileId)) {
            response.setContentType(record.getFileType() != null ? record.getFileType() : "application/octet-stream");
            is.transferTo(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
