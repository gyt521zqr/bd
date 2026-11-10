package com.magao.backend.module.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频弹幕实体类
 * <p>
 * 对应数据库表：video_danmaku
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("video_danmaku")
public class VideoDanmaku implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long videoId;
    private Long userId;
    private String username;
    /** 弹幕内容 */
    private String content;
    /** 弹幕出现时间（秒） */
    private Integer timePosition;
    /** 弹幕颜色 */
    private String color;
    private LocalDateTime createTime;
}
