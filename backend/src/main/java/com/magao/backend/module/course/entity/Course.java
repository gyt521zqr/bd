package com.magao.backend.module.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 课程实体类
 * <p>
 * 对应数据库表：course_info
 * 存储课程基本信息
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_info")
public class Course extends BaseEntity {
    /** 课程标题 */
    private String title;
    /** 课程副标题 */
    private String subtitle;
    /** 课程封面图URL */
    private String coverImage;
    /** 课程简介 */
    private String description;
    /** 课程详情（富文本） */
    private String content;
    /** 讲师ID */
    private Long teacherId;
    /** 讲师名称 */
    private String teacherName;
    /** 课程价格 */
    private BigDecimal price;
    /** 原价 */
    private BigDecimal originalPrice;
    /** 课程状态（0-草稿，1-已发布，2-已下架，3-已归档） */
    private Integer status;
    /** 课程分类ID */
    private Long categoryId;
    /** 课程标签（逗号分隔） */
    private String tags;
    /** 总课时数 */
    private Integer totalLessons;
    /** 总时长（秒） */
    private Integer totalDuration;
    /** 学习人数 */
    private Integer studentCount;
    /** 评分 */
    private BigDecimal rating;
    /** 排序号 */
    private Integer sortOrder;
}
