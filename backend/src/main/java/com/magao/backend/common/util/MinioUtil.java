package com.magao.backend.common.util;

import com.magao.backend.common.config.MinioConfig;
import com.magao.backend.common.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件操作工具类
 * <p>
 * 封装MinIO对象存储的常用操作：
 * 1. 文件上传（支持MultipartFile和InputStream）
 * 2. 文件下载（获取文件输入流）
 * 3. 文件删除
 * 4. 获取临时访问URL
 * 5. 检查存储桶是否存在（不存在则创建）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class MinioUtil {

    /** MinIO客户端 */
    private final MinioClient minioClient;

    /** MinIO配置 */
    private final MinioConfig minioConfig;

    /**
     * 上传文件
     * <p>
     * 将MultipartFile上传到MinIO指定存储桶中，
     * 自动生成UUID文件名避免冲突。
     * </p>
     *
     * @param file     上传的文件
     * @param bucket   存储桶名称（为空则使用默认存储桶）
     * @param folder   文件夹路径（如：avatar/、course/）
     * @return 文件在MinIO中的完整路径（objectName）
     */
    public String upload(MultipartFile file, String bucket, String folder) {
        try {
            // 如果未指定存储桶，使用默认存储桶
            String bucketName = (bucket == null || bucket.isEmpty()) ? minioConfig.getDefaultBucket() : bucket;
            // 确保存储桶存在
            ensureBucketExists(bucketName);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = folder + UUID.randomUUID().toString().replace("-", "") + extension;

            // 执行上传
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }

            log.info("文件上传成功：bucket={}, object={}", bucketName, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件（使用默认存储桶）
     *
     * @param file   上传的文件
     * @param folder 文件夹路径
     * @return 文件在MinIO中的完整路径
     */
    public String upload(MultipartFile file, String folder) {
        return upload(file, null, folder);
    }

    /**
     * 下载文件
     * <p>
     * 从MinIO获取文件的输入流
     * </p>
     *
     * @param objectName 文件对象名称
     * @param bucket     存储桶名称（为空则使用默认存储桶）
     * @return 文件输入流
     */
    public InputStream download(String objectName, String bucket) {
        try {
            String bucketName = (bucket == null || bucket.isEmpty()) ? minioConfig.getDefaultBucket() : bucket;
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败：{}", e.getMessage(), e);
            throw new BusinessException("文件下载失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 文件对象名称
     * @param bucket     存储桶名称（为空则使用默认存储桶）
     */
    public void delete(String objectName, String bucket) {
        try {
            String bucketName = (bucket == null || bucket.isEmpty()) ? minioConfig.getDefaultBucket() : bucket;
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("文件删除成功：bucket={}, object={}", bucketName, objectName);
        } catch (Exception e) {
            log.error("文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件临时访问URL
     * <p>
     * 生成一个带签名的临时URL，用于前端直接访问MinIO中的文件
     * </p>
     *
     * @param objectName 文件对象名称
     * @param bucket     存储桶名称（为空则使用默认存储桶）
     * @param expiry    过期时间（秒）
     * @return 临时访问URL字符串
     */
    public String getPresignedUrl(String objectName, String bucket, int expiry) {
        try {
            String bucketName = (bucket == null || bucket.isEmpty()) ? minioConfig.getDefaultBucket() : bucket;
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("获取临时URL失败：{}", e.getMessage(), e);
            throw new BusinessException("获取文件访问链接失败：" + e.getMessage());
        }
    }

    /**
     * 确保存储桶存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     */
    private void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建存储桶：{}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查/创建存储桶失败：{}", e.getMessage(), e);
            throw new BusinessException("存储桶操作失败：" + e.getMessage());
        }
    }
}
