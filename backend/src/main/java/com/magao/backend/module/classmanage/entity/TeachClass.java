package com.magao.backend.module.classmanage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级实体类
 * <p>
 * 对应数据库表：teach_class
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teach_class")
public class TeachClass extends BaseEntity {
    /** 班级名称 */
    private String className;
    /** 班级描述 */
    private String description;
    /** 教师ID */
    private Long teacherId;
    /** 课程ID */
    private Long courseId;
    /** 最大学生数 */
    private Integer maxStudent;
    /** 当前学生数 */
    private Integer currentStudent;
    /** 状态（0-未开班，1-进行中，2-已结课） */
    private Integer status;
}
