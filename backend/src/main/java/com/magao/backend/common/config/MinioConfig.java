package com.magao.backend.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO对象存储配置类
 * <p>
 * 功能说明：
 * 1. 读取MinIO相关配置属性
 * 2. 创建MinioClient Bean供全局使用
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
public class MinioConfig {

    /**
     * MinIO服务地址（如：http://localhost:9000）
     */
    private String endpoint;

    /**
     * MinIO访问密钥（Access Key）
     */
    private String accessKey;

    /**
     * MinIO秘密密钥（Secret Key）
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String defaultBucket;

    /**
     * 创建MinioClient实例
     * <p>
     * 使用配置中的endpoint、accessKey、secretKey构建MinIO客户端
     * </p>
     *
     * @return MinioClient实例
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
