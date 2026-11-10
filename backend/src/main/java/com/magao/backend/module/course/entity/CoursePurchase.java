package com.magao.backend.module.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 课程购买记录实体类
 * <p>
 * 对应数据库表：course_purchase
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_purchase")
public class CoursePurchase extends BaseEntity {
    /** 用户ID */
    private Long userId;
    /** 课程ID */
    private Long courseId;
    /** 订单ID */
    private Long orderId;
    /** 支付金额 */
    private BigDecimal payAmount;
    /** 过期时间 */
    private java.time.LocalDateTime expireTime;
}
