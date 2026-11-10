package com.magao.backend.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购活动实体类
 * <p>
 * 对应数据库表：group_buy_activity
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_buy_activity")
public class GroupBuyActivity extends BaseEntity {
    /** 活动名称 */
    private String activityName;
    /** 关联课程ID */
    private Long courseId;
    /** 原价 */
    private BigDecimal originalPrice;
    /** 团购价 */
    private BigDecimal groupPrice;
    /** 成团人数 */
    private Integer groupSize;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 状态（0-未开始，1-进行中，2-已结束） */
    private Integer status;
}
