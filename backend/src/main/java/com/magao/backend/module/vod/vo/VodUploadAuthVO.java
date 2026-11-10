package com.magao.backend.module.vod.vo;

import lombok.Data;

/**
 * VOD 上传凭证响应 VO
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class VodUploadAuthVO {
    /** 上传凭证（Token） */
    private String uploadToken;

    /** 上传地址 */
    private String uploadAddress;

    /** 视频 ID（上传后用于播放） */
    private String vid;

    /** 存储空间 */
    private String spaceName;
}