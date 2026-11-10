package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 试卷实体类
 * <p>
 * 对应数据库表：exam_paper
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_paper")
public class ExamPaper extends BaseEntity {
    /** 试卷名称 */
    private String paperName;
    /** 考试ID */
    private Long examId;
    /** 总分 */
    private Integer totalScore;
    /** 及格分 */
    private Integer passScore;
    /** 题目数量 */
    private Integer questionCount;
}
