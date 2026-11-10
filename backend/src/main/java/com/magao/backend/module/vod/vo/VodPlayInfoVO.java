package com.magao.backend.module.vod.vo;

import lombok.Data;

import java.util.List;

/**
 * VOD 播放信息响应 VO
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class VodPlayInfoVO {
    /** 视频 ID */
    private String vid;

    /** 视频标题 */
    private String title;

    /** 视频封面 URL */
    private String coverUrl;

    /** 视频时长（秒） */
    private Float duration;

    /** 视频状态 */
    private String status;

    /** 播放地址列表（不同清晰度） */
    private List<PlayUrl> playUrls;

    @Data
    public static class PlayUrl {
        /** 清晰度（流畅、标清、高清、超清） */
        private String definition;

        /** 播放地址 */
        private String url;

        /** 视频格式 */
        private String format;
    }
}