package com.magao.backend.module.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件记录实体类
 * <p>
 * 对应数据库表：file_record
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_record")
public class FileRecord extends BaseEntity {
    /** 文件名 */
    private String fileName;
    /** 原始文件名 */
    private String originalName;
    /** 文件路径（MinIO objectName） */
    private String filePath;
    /** 文件URL */
    private String fileUrl;
    /** 文件大小（字节） */
    private Long fileSize;
    /** 文件类型（MIME） */
    private String fileType;
    /** 文件扩展名 */
    private String extension;
    /** 存储桶 */
    private String bucket;
    /** 上传用户ID */
    private Long uploadUserId;
    /** 业务类型（avatar-头像，course-课程，exam-考试等） */
    private String bizType;
    /** 业务ID */
    private Long bizId;
}
