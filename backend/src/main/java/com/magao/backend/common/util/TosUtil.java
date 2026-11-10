package com.magao.backend.common.util;

import com.magao.backend.common.config.TosConfig;
import com.magao.backend.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

/**
 * 火山引擎 TOS 文件操作工具类
 * <p>
 * TOS（Tinder Object Storage）兼容 AWS S3 协议，
 * 使用 AWS SDK for Java v2 进行对象存储操作。
 * 功能：
 * 1. 文件上传（支持 MultipartFile）
 * 2. 文件下载（获取输入流）
 * 3. 文件删除
 * 4. 获取预签名临时访问 URL
 * 5. 检查并创建存储桶
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "tos.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class TosUtil {

    private final TosConfig tosConfig;

    /** S3 客户端 */
    private S3Client s3Client;

    /** 预签名 URL 生成器 */
    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        var credentials = AwsBasicCredentials.create(tosConfig.getAccessKey(), tosConfig.getSecretKey());
        var provider = StaticCredentialsProvider.create(credentials);

        // 构建 S3 客户端（兼容 TOS 端点）
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(tosConfig.getEndpoint()))
                .region(Region.of(tosConfig.getRegion()))
                .credentialsProvider(provider)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        // 构建预签名 URL 生成器
        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(tosConfig.getEndpoint()))
                .region(Region.of(tosConfig.getRegion()))
                .credentialsProvider(provider)
                .build();

        log.info("TOS 客户端初始化完成，endpoint={}, bucket={}", tosConfig.getEndpoint(), tosConfig.getDefaultBucket());
    }

    /**
     * 上传文件到 TOS
     *
     * @param file   上传的文件
     * @param bucket 存储桶名称（为空则使用默认存储桶）
     * @param folder 文件夹路径（如：avatar/、course/）
     * @return 文件在 TOS 中的完整路径（objectKey）
     */
    public String upload(MultipartFile file, String bucket, String folder) {
        try {
            String bucketName = (bucket == null || bucket.isEmpty()) ? tosConfig.getDefaultBucket() : bucket;
            ensureBucketExists(bucketName);

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectKey = folder + UUID.randomUUID().toString().replace("-", "") + extension;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("TOS 文件上传成功：bucket={}, key={}", bucketName, objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("TOS 文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件到 TOS（使用默认存储桶）
     */
    public String upload(MultipartFile file, String folder) {
        return upload(file, null, folder);
    }

    /**
     * 上传字节数据到 TOS（用于前端传来的 base64 封面等场景）
     *
     * @param data        文件字节数据
     * @param contentType 内容类型（如 image/png）
     * @param bucket      存储桶名称（为空则使用默认存储桶）
     * @param folder      文件夹路径（如：cover/）
     * @return 文件在 TOS 中的完整路径（objectKey）
     */
    public String upload(byte[] data, String contentType, String bucket, String folder) {
        try {
            if (data == null || data.length == 0) {
                throw new BusinessException("上传数据不能为空");
            }
            String bucketName = (bucket == null || bucket.isEmpty()) ? tosConfig.getDefaultBucket() : bucket;
            ensureBucketExists(bucketName);

            String objectKey = folder + UUID.randomUUID().toString().replace("-", "") + ".png";

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));

            log.info("TOS 字节上传成功：bucket={}, key={}", bucketName, objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("TOS 字节上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 从 TOS 下载文件
     *
     * @param objectKey 文件对象键
     * @param bucket    存储桶名称
     * @return 文件输入流
     */
    public InputStream download(String objectKey, String bucket) {
        try {
            String bucketName = (bucket == null || bucket.isEmpty()) ? tosConfig.getDefaultBucket() : bucket;
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            return s3Client.getObject(getRequest);
        } catch (Exception e) {
            log.error("TOS 文件下载失败：{}", e.getMessage(), e);
            throw new BusinessException("文件下载失败：" + e.getMessage());
        }
    }

    /**
     * 从 TOS 删除文件
     *
     * @param objectKey 文件对象键
     * @param bucket    存储桶名称
     */
    public void delete(String objectKey, String bucket) {
        try {
            String bucketName = (bucket == null || bucket.isEmpty()) ? tosConfig.getDefaultBucket() : bucket;
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("TOS 文件删除成功：bucket={}, key={}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("TOS 文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件预签名临时访问 URL
     *
     * @param objectKey 文件对象键
     * @param bucket    存储桶名称
     * @param expirySec 过期时间（秒）
     * @return 临时访问 URL
     */
    public String getPresignedUrl(String objectKey, String bucket, int expirySec) {
        try {
            String bucketName = (bucket == null || bucket.isEmpty()) ? tosConfig.getDefaultBucket() : bucket;
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirySec))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("TOS 获取预签名 URL 失败：{}", e.getMessage(), e);
            throw new BusinessException("获取文件访问链接失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件的公开访问 URL（当 TOS 存储桶开启了公开读权限时）
     *
     * @param objectKey 文件对象键
     * @param bucket    存储桶名称
     * @return 公开访问 URL
     */
    public String getPublicUrl(String objectKey, String bucket) {
        String bucketName = (bucket == null || bucket.isEmpty()) ? tosConfig.getDefaultBucket() : bucket;
        String domain = tosConfig.getPublicDomain();
        if (domain != null && !domain.isEmpty()) {
            return domain + "/" + objectKey;
        }
        // 没有配置 CDN 域名时，使用 TOS 端点直接访问
        return tosConfig.getEndpoint().replace("://", "://" + bucketName + ".") + "/" + objectKey;
    }

    /**
     * 确保存储桶存在，不存在则创建
     */
    private void ensureBucketExists(String bucketName) {
        try {
            headBucket(bucketName);
        } catch (NoSuchBucketException e) {
            try {
                CreateBucketRequest createRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createRequest);
                log.info("TOS 存储桶已创建：{}", bucketName);
            } catch (Exception ex) {
                log.error("TOS 创建存储桶失败：{}", ex.getMessage());
                throw new BusinessException("存储桶操作失败：" + ex.getMessage());
            }
        } catch (Exception e) {
            log.error("TOS 检查存储桶失败：{}", e.getMessage());
            throw new BusinessException("存储桶操作失败：" + e.getMessage());
        }
    }

    private void headBucket(String bucketName) {
        // 尝试获取桶信息，不存在会抛出 NoSuchBucketException
        HeadBucketRequest headRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
        s3Client.headBucket(headRequest);
    }
}