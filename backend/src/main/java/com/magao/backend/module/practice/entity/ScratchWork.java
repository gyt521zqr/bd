package com.magao.backend.module.practice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Scratch 自由创作作品实体类
 * <p>
 * 对应数据库表：scratch_work
 * </p>
 *
 * @author Magao开发团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scratch_work")
public class ScratchWork extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 作品名称 */
    private String title;

    /** 作品描述 */
    private String description;

    /** sb3 文件在 MinIO 中的存储路径 */
    private String sb3FileKey;

    /** 缩略图在 MinIO 中的存储路径 */
    private String thumbnailKey;

    /** sb3 文件大小（字节） */
    private Long fileSize;

    /** 是否公开（0-私有，1-公开） */
    private Integer isPublic;

    /** 点赞数 */
    private Integer likeCount;

    /** 浏览数 */
    private Integer viewCount;
}
