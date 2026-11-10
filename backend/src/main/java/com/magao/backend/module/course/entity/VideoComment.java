package com.magao.backend.module.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频评论实体类
 * <p>
 * 对应数据库表：video_comment
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video_comment")
public class VideoComment extends BaseEntity {
    /** 视频ID */
    private Long videoId;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 用户头像 */
    private String avatar;
    /** 评论内容 */
    private String content;
    /** 父评论ID（0为顶级评论） */
    private Long parentId;
    /** 点赞数 */
    private Integer likeCount;
}
