package com.magao.backend.module.vod.controller;

import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 视频点播（VOD）控制器
 * <p>
 * 对齐前端 src/api/vod.ts 的全部路由。
 * 说明：真实的云端 VOD（火山引擎）需要 AccessKey/SecretKey 与 SpaceName 配置，
 * 在未配置 {@code vod.enabled=true} 且填入凭据前，本控制器以"优雅降级"方式返回
 * 占位数据，保证前端路由全部命中（不 404）、页面可正常渲染；课程视频的真实播放
 * 由 {@code GET /api/course/video/{id}/play} 提供。后续接入真实云端 VOD 时，
 * 可在此注入 VodService 替换占位逻辑。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "视频点播", description = "VOD 上传凭证、播放信息等接口（占位降级实现）")
@RestController
@RequestMapping("/api/vod")
public class VodController extends BaseController {

    @Operation(summary = "获取视频上传凭证")
    @PostMapping("/upload-auth")
    public Result<Map<String, Object>> getUploadAuth(@RequestParam String fileName,
                                                       @RequestParam(required = false) String title) {
        String videoId = "vod-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uploadAddress", "");
        data.put("uploadAuth", "");
        data.put("videoId", videoId);
        data.put("requestId", UUID.randomUUID().toString());
        log.warn("VOD 未配置云端凭据，upload-auth 返回占位数据（fileName={}）", fileName);
        return Result.success(data);
    }

    @Operation(summary = "刷新上传凭证")
    @PostMapping("/refresh-upload-auth")
    public Result<Map<String, Object>> refreshUploadAuth(@RequestParam String videoId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uploadAddress", "");
        data.put("uploadAuth", "");
        data.put("videoId", videoId);
        data.put("requestId", UUID.randomUUID().toString());
        return Result.success(data);
    }

    @Operation(summary = "获取视频播放地址")
    @GetMapping("/play-info/{videoId}")
    public Result<Map<String, Object>> getPlayInfo(@PathVariable String videoId) {
        return Result.success(buildPlayInfo(videoId));
    }

    @Operation(summary = "批量获取视频播放地址")
    @PostMapping("/batch-play-info")
    public Result<Map<String, Map<String, Object>>> batchPlayInfo(@RequestBody Map<String, Object> body) {
        Object idsObj = body.get("videoIds");
        List<String> ids = new ArrayList<>();
        if (idsObj instanceof List) {
            for (Object o : (List<?>) idsObj) {
                ids.add(String.valueOf(o));
            }
        }
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String id : ids) {
            result.put(id, buildPlayInfo(id));
        }
        return Result.success(result);
    }

    @Operation(summary = "获取视频列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) Integer pageNum,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String status) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", Collections.emptyList());
        data.put("total", 0);
        return Result.success(data);
    }

    @Operation(summary = "删除视频")
    @DeleteMapping("/{videoId}")
    public Result<Void> deleteVideo(@PathVariable String videoId) {
        log.info("VOD 删除（占位）：videoId={}", videoId);
        return Result.success();
    }

    @Operation(summary = "获取视频播放凭证（加密播放）")
    @GetMapping("/play-auth/{videoId}")
    public Result<Map<String, Object>> getPlayAuth(@PathVariable String videoId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("playAuth", "");
        data.put("expiration", 0);
        return Result.success(data);
    }

    @Operation(summary = "转码任务查询")
    @GetMapping("/transcode-status/{videoId}")
    public Result<Map<String, Object>> transcodeStatus(@PathVariable String videoId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("videoId", videoId);
        data.put("status", "Success");
        data.put("progress", 100);
        data.put("definitionList", Collections.emptyList());
        return Result.success(data);
    }

    private Map<String, Object> buildPlayInfo(String videoId) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("videoId", videoId);
        info.put("title", "");
        info.put("duration", 0);
        info.put("coverUrl", "");
        info.put("status", "Success");
        info.put("createTime", LocalDateTime.now().toString());
        Map<String, String> playUrls = new LinkedHashMap<>();
        playUrls.put("ld", "");
        playUrls.put("sd", "");
        playUrls.put("hd", "");
        playUrls.put("fhd", "");
        info.put("playUrls", playUrls);
        info.put("defaultUrl", "");
        return info;
    }
}
