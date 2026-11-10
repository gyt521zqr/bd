package com.magao.backend.module.competition.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 竞赛信息实体类
 * <p>
 * 对应数据库表：competition_info
 * </p>
 *
 * @author Magao开发团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("competition_info")
public class CompetitionInfo extends BaseEntity {

    /** 竞赛标题 */
    private String title;

    /** 竞赛描述 */
    private String description;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 竞赛状态（0-未开始，1-进行中，2-已结束） */
    private Integer status;

    /** 允许的积木分类（JSON数组，如 ["motion","control","operator"]） */
    private String allowedBlockCategories;

    /** 最大参赛人数 */
    private Integer maxParticipants;

    /** 创建者ID */
    private Long creatorId;
}
