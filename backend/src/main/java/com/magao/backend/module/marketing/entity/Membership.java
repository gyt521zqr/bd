package com.magao.backend.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 会员实体类
 * <p>
 * 对应数据库表：membership
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("membership")
public class Membership extends BaseEntity {
    /** 会员名称 */
    private String membershipName;
    /** 会员等级（1-月度，2-季度，3-年度） */
    private Integer level;
    /** 价格 */
    private BigDecimal price;
    /** 有效期（天） */
    private Integer validDays;
    /** 折扣比例（如80表示8折） */
    private Integer discount;
    /** 状态（0-下架，1-上架） */
    private Integer status;
    /** 描述 */
    private String description;
}
