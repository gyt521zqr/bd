package com.magao.backend.module.course.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程列表视图对象
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class CourseVO {
    private Long id;
    private String title;
    private String subtitle;
    private String coverImage;
    private String description;
    private Long teacherId;
    private String teacherName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer status;
    private Long categoryId;
    private String tags;
    private Integer totalLessons;
    private Integer totalDuration;
    private Integer studentCount;
    private BigDecimal rating;
    private LocalDateTime createTime;
}
