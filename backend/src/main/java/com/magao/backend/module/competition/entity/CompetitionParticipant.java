package com.magao.backend.module.competition.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 竞赛参赛者实体类
 * <p>
 * 对应数据库表：competition_participant
 * </p>
 *
 * @author Magao开发团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("competition_participant")
public class CompetitionParticipant extends BaseEntity {

    /** 竞赛ID */
    private Long competitionId;

    /** 用户ID */
    private Long userId;

    /** 加入时间 */
    private LocalDateTime joinTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 得分 */
    private Integer score;

    /** 排名 */
    private Integer ranking;

    /** 违规次数 */
    private Integer violationCount;
}
