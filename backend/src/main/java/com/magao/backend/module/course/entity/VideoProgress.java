package com.magao.backend.module.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频学习进度实体类
 * <p>
 * 对应数据库表：video_progress
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("video_progress")
public class VideoProgress implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long videoId;
    /** 已观看时长（秒） */
    private Integer watchedDuration;
    /** 是否已看完（0-否，1-是） */
    private Integer isCompleted;
    /** 最后观看时间 */
    private LocalDateTime lastWatchTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
