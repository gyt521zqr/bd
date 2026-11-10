package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 考试记录实体类
 * <p>
 * 对应数据库表：exam_record
 * 记录每个学生每次考试的整体情况
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_record")
public class ExamRecord extends BaseEntity {
    /** 考试ID */
    private Long examId;
    /** 用户ID */
    private Long userId;
    /** 试卷ID */
    private Long paperId;
    /** 开始答题时间 */
    private LocalDateTime startTime;
    /** 提交时间 */
    private LocalDateTime submitTime;
    /** 考试状态（0-未开始，1-答题中，2-已交卷，3-已批改） */
    private Integer status;
    /** 得分 */
    private Integer score;
    /** 是否及格（0-不及格，1-及格） */
    private Integer isPassed;
    /** 切屏次数 */
    private Integer switchScreenCount;
    /** 离开页面次数 */
    private Integer leavePageCount;
    /** 违规标记（0-正常，1-有违规） */
    private Integer violationFlag;
}
