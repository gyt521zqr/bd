package com.magao.backend.module.competition.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 竞赛提交实体类
 * <p>
 * 对应数据库表：competition_submit（按月分表 competition_submit_YYYYMM）
 * 注意：分表实体不继承 BaseEntity，自行管理字段
 * </p>
 *
 * @author Magao开发团队
 */
@Data
@TableName("competition_submit")
public class CompetitionSubmit implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 竞赛ID */
    private Long competitionId;

    /** 用户ID */
    private Long userId;

    /** Scratch sb3 数据（JSON） */
    private String sb3Data;

    /** 判题结果（JSON） */
    private String judgeResult;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 提交IP地址 */
    private String ipAddress;
}
