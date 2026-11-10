package com.magao.backend.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 * <p>
 * 对应数据库表：coupon
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {
    /** 优惠券名称 */
    private String couponName;
    /** 优惠券类型（1-满减券，2-折扣券，3-无门槛券） */
    private Integer couponType;
    /** 满减门槛金额 */
    private BigDecimal minAmount;
    /** 优惠金额/折扣值 */
    private BigDecimal discountValue;
    /** 发放总量 */
    private Integer totalCount;
    /** 已领取数量 */
    private Integer receivedCount;
    /** 有效期开始 */
    private LocalDateTime validStartTime;
    /** 有效期结束 */
    private LocalDateTime validEndTime;
    /** 状态（0-停用，1-启用） */
    private Integer status;
}
