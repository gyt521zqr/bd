package com.magao.backend.module.course.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 课程详情视图对象
 * <p>
 * 包含课程基本信息和视频列表（扁平结构，无章节）。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseDetailVO extends CourseVO {
    /** 课程详情（富文本） */
    private String content;
    /** 视频列表 */
    private List<VideoVO> videos;

    @Data
    public static class VideoVO {
        private Long id;
        private String title;
        private String videoUrl;
        private Integer sortOrder;
    }
}
