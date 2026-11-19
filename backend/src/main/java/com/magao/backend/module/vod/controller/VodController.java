package com.magao.backend.module.vod.controller;

import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.config.VodConfig;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.vod.service.VodService;
import com.magao.backend.module.vod.vo.VodPlayInfoVO;
import com.magao.backend.module.vod.vo.VodUploadAuthVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 视频点播（VOD）控制器
 * <p>
 * 对齐前端 src/api/vod.ts 的全部路由。P0-09：注入真实 VodService（ vod.enabled=true 时存在），
 * 未启用/未配置凭据时明确返回错误，不再用空字符串占位"假装成功"误导前端。
 * 课程视频的真实播放仍由 {@code GET /api/course/video/{id}/play} 提供。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "视频点播", description = "VOD 上传凭证、播放信息等接口")
@RestController
@RequestMapping("/api/vod")
@RequiredArgsConstructor
public class VodController extends BaseController {

    /** 可选注入：vod.enabled=false 时 VodService bean 不存在 */
    private final ObjectProvider<VodService> vodServiceProvider;
    private final VodConfig vodConfig;

    /** 取得真实 VOD 服务，未启用则明确报错（不返回假凭证） */
    private VodService requireVodService() {
        if (!vodConfig.isEnabled()) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE, "视频点播服务未启用，请联系管理员配置 VOD 凭据");
        }
        VodService svc = vodServiceProvider.getIfAvailable();
        if (svc == null) {
            throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE, "视频点播服务不可用");
        }
        return svc;
    }

    @Operation(summary = "获取视频上传凭证")
    @PostMapping("/upload-auth")
    public Result<Map<String, Object>> getUploadAuth(@RequestParam String fileName,
                                                       @RequestParam(required = false) String title) {
        VodUploadAuthVO vo = requireVodService().getUploadAuth(fileName, title);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uploadToken", vo.getUploadToken());
        data.put("uploadAddress", vo.getUploadAddress());
        data.put("videoId", vo.getVid());
        data.put("spaceName", vo.getSpaceName());
        data.put("requestId", UUID.randomUUID().toString());
        return Result.success(data);
    }

    @Operation(summary = "刷新上传凭证")
    @PostMapping("/refresh-upload-auth")
    public Result<Map<String, Object>> refreshUploadAuth(@RequestParam String videoId,
                                                          @RequestParam(required = false) String fileName) {
        // 火山 VOD 上传凭证有时效，刷新即重新签发
        VodUploadAuthVO vo = requireVodService().getUploadAuth(
                fileName == null || fileName.isBlank() ? videoId : fileName, videoId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uploadToken", vo.getUploadToken());
        data.put("uploadAddress", vo.getUploadAddress());
        data.put("videoId", vo.getVid());
        data.put("requestId", UUID.randomUUID().toString());
        return Result.success(data);
    }

    @Operation(summary = "获取视频播放地址")
    @GetMapping("/play-info/{videoId}")
    public Result<Map<String, Object>> getPlayInfo(@PathVariable String videoId) {
        return Result.success(buildPlayInfo(requireVodService().getPlayInfo(videoId)));
    }

    @Operation(summary = "批量获取视频播放地址")
    @PostMapping("/batch-play-info")
    public Result<Map<String, Map<String, Object>>> batchPlayInfo(@RequestBody Map<String, Object> body) {
        VodService svc = requireVodService();
        Object idsObj = body.get("videoIds");
        List<String> ids = new ArrayList<>();
        if (idsObj instanceof List) {
            for (Object o : (List<?>) idsObj) {
                ids.add(String.valueOf(o));
            }
        }
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (String id : ids) {
            result.put(id, buildPlayInfo(svc.getPlayInfo(id)));
        }
        return Result.success(result);
    }

    @Operation(summary = "获取视频列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) Integer pageNum,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String status) {
        // 火山 VOD 列表需服务端列目录 API，当前 VodService 未暴露；明确提示而非返回空列表假装成功
        throw new BusinessException(ErrorCode.SERVICE_UNAVAILABLE, "视频列表查询暂未开放");
    }

    @Operation(summary = "删除视频")
    @DeleteMapping("/{videoId}")
    public Result<Void> deleteVideo(@PathVariable String videoId) {
        requireVodService().deleteVideo(videoId);
        return Result.success();
    }

    @Operation(summary = "获取视频播放凭证（加密播放）")
    @GetMapping("/play-auth/{videoId}")
    public Result<Map<String, Object>> getPlayAuth(@PathVariable String videoId) {
        // 播放凭证等同 play-info 的 playUrls；统一走真实服务
        Map<String, Object> info = buildPlayInfo(requireVodService().getPlayInfo(videoId));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("playAuth", info.get("defaultUrl"));
        data.put("playInfo", info);
        data.put("expiration", 3600);
        return Result.success(data);
    }

    @Operation(summary = "转码任务查询")
    @GetMapping("/transcode-status/{videoId}")
    public Result<Map<String, Object>> transcodeStatus(@PathVariable String videoId) {
        VodPlayInfoVO vo = requireVodService().getPlayInfo(videoId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("videoId", videoId);
        data.put("status", vo.getStatus() == null ? "Unknown" : vo.getStatus());
        data.put("definitionList", vo.getPlayUrls() == null ? Collections.emptyList() : vo.getPlayUrls());
        return Result.success(data);
    }

    /** VodPlayInfoVO -> 前端期望的 Map 结构 */
    private Map<String, Object> buildPlayInfo(VodPlayInfoVO vo) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("videoId", vo.getVid());
        info.put("title", vo.getTitle());
        info.put("duration", vo.getDuration());
        info.put("coverUrl", vo.getCoverUrl());
        info.put("status", vo.getStatus());
        Map<String, String> playUrls = new LinkedHashMap<>();
        if (vo.getPlayUrls() != null) {
            for (VodPlayInfoVO.PlayUrl p : vo.getPlayUrls()) {
                if (p.getDefinition() != null) playUrls.put(p.getDefinition(), p.getUrl());
            }
        }
        info.put("playUrls", playUrls);
        info.put("defaultUrl", playUrls.isEmpty() ? "" : playUrls.values().iterator().next());
        return info;
    }
}
