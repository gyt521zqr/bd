package com.magao.backend.module.practice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Scratch项目实体类
 * <p>
 * 对应数据库表：scratch_project
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scratch_project")
public class ScratchProject extends BaseEntity {
    /** 用户ID */
    private Long userId;
    /** 项目名称 */
    private String projectName;
    /** 项目描述 */
    private String description;
    /** 项目文件URL（.sb3文件） */
    private String projectUrl;
    /** 缩略图URL */
    private String thumbnailUrl;
    /** 是否公开（0-私有，1-公开） */
    private Integer isPublic;
    /** 点赞数 */
    private Integer likeCount;
    /** 浏览数 */
    private Integer viewCount;
}
