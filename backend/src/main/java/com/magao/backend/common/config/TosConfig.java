package com.magao.backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 火山引擎 TOS（对象存储）配置类
 * <p>
 * TOS 兼容 AWS S3 协议，使用 S3 SDK 进行访问。
 * 配置项从 application.yml 的 tos 前缀读取。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tos")
public class TosConfig {

    /** TOS 访问端点（如：https://tos-cn-beijing.volces.com） */
    private String endpoint;

    /** 区域（如：cn-beijing） */
    private String region;

    /** 访问密钥 ID */
    private String accessKey;

    /** 秘密访问密钥 */
    private String secretKey;

    /** 默认存储桶名称 */
    private String defaultBucket;

    /** 文件访问域名（开启 CDN 加速后使用） */
    private String publicDomain;
}