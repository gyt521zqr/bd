package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试批改复核实体类
 * <p>
 * 对应数据库表：exam_grading_review
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("exam_grading_review")
public class ExamGradingReview implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long detailId;
    private Long reviewerId;
    /** 复核结果（0-待复核，1-同意，2-驳回修改） */
    private Integer reviewResult;
    private String reviewComment;
    private LocalDateTime createTime;
}
