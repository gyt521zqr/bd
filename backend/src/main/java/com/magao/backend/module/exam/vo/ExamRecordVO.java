package com.magao.backend.module.exam.vo;

import com.magao.backend.module.exam.entity.ExamRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试记录视图对象（含考试标题/总分/及格分）
 * <p>
 * 考试记录列表展示需要考试标题与总分，但 ExamRecord 仅存 examId，需关联 exam_info 补齐。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamRecordVO extends ExamRecord {
    /** 考试标题（关联 exam_info 补齐） */
    private String examTitle;
    /** 试卷总分 */
    private Integer totalScore;
    /** 及格分 */
    private Integer passScore;
}
