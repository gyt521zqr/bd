package com.magao.backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 火山引擎 VOD（视频点播）配置类
 * <p>
 * 配置项从 application.yml 的 vod 前缀读取。
 * VOD 提供视频上传、转码、播放等功能。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "vod")
public class VodConfig {

    /** 是否启用真实火山 VOD（false 时所有 VOD 端点明确报未配置，不返回假凭证） */
    private boolean enabled = false;

    /** 火山引擎账号 Access Key */
    private String accessKey;

    /** 火山引擎账号 Secret Key */
    private String secretKey;

    /** VOD 服务区域（如：cn-north-1） */
    private String region;

    /** 视频播放域名（CDN 加速域名） */
    private String playDomain;

    /** 视频转码模板 ID（默认空字符串表示不转码） */
    private String transcodeTemplateId;

    /** 空间名称（VOD 中创建的存储空间） */
    private String spaceName;
}