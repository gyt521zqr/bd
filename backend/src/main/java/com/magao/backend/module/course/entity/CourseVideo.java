package com.magao.backend.module.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程视频实体类
 * <p>
 * 对应数据库表：course_video
 * 视频直接挂载在课程下（无章节层级），仅保留名称与视频地址，便于维护。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_video")
public class CourseVideo extends BaseEntity {
    /** 课程ID */
    private Long courseId;
    /** 视频名称 */
    private String title;
    /** 视频地址 */
    private String videoUrl;
    /** 排序号 */
    private Integer sortOrder;
}
