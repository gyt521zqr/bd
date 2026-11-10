package com.magao.backend.module.practice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 练习提交记录实体类
 * <p>
 * 对应数据库表：practice_record
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("practice_record")
public class PracticeRecord extends BaseEntity {
    /** 用户ID */
    private Long userId;
    /** 题目ID */
    private Long questionId;
    /** 提交代码 */
    private String code;
    /** 编程语言 */
    private Integer language;
    /** 评测状态（0-待评测，1-评测中，2-通过，3-答案错误等） */
    private Integer judgeStatus;
    /** 执行时间（毫秒） */
    private Integer executeTime;
    /** 内存消耗（KB） */
    private Integer memoryUsage;
    /** 通过用例数 */
    private Integer passCaseCount;
    /** 总用例数 */
    private Integer totalCaseCount;
    /** 评测结果详情（JSON） */
    private String judgeResult;
    /** Judge0提交ID */
    private String judge0Token;
    /** Scratch作品封面缩略图URL（可选） */
    private String coverUrl;
}
