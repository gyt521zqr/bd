package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试批改任务实体类
 * <p>
 * 对应数据库表：exam_grading_task
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_grading_task")
public class ExamGradingTask extends BaseEntity {
    /** 考试ID */
    private Long examId;
    /** 批改教师ID */
    private Long teacherId;
    /** 批改状态（0-待批改，1-批改中，2-已完成） */
    private Integer status;
    /** 已批改数量 */
    private Integer gradedCount;
    /** 总数量 */
    private Integer totalCount;
}
