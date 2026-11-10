package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 考试信息实体类
 * <p>
 * 对应数据库表：exam_info
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_info")
public class ExamInfo extends BaseEntity {
    /** 考试标题 */
    private String title;
    /** 考试描述 */
    private String description;
    /** 考试类型（1-随堂测验，2-单元测试，3-期中考试，4-期末考试，5-竞赛） */
    private Integer examType;
    /** 课程ID */
    private Long courseId;
    /** 创建教师ID */
    private Long teacherId;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 考试时长（分钟） */
    private Integer duration;
    /** 总分 */
    private Integer totalScore;
    /** 及格分数 */
    private Integer passScore;
    /** 考试状态（0-未开始，1-进行中，2-已结束，3-已批改） */
    private Integer status;
    /** 防作弊开关（0-关闭，1-开启） */
    private Integer antiCheat;
    /** 最大切屏次数 */
    private Integer maxSwitchScreen;
    /** 是否显示答案（0-不显示，1-考试结束后显示） */
    private Integer showAnswer;
}
