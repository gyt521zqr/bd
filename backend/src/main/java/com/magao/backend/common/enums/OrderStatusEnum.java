package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 * <p>
 * 定义订单的生命周期状态：
 * - PENDING：待支付（订单已创建，等待用户支付）
 * - PAID：已支付（用户已完成支付）
 * - CANCELLED：已取消（用户主动取消或系统自动取消）
 * - REFUNDING：退款中（用户申请退款，等待处理）
 * - REFUNDED：已退款（退款已完成）
 * - EXPIRED：已过期（超时未支付自动关闭）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    /** 待支付 */
    PENDING(0, "待支付"),
    /** 已支付 */
    PAID(1, "已支付"),
    /** 已取消 */
    CANCELLED(2, "已取消"),
    /** 退款中 */
    REFUNDING(3, "退款中"),
    /** 已退款 */
    REFUNDED(4, "已退款"),
    /** 已过期 */
    EXPIRED(5, "已过期");

    /** 状态值（存储到数据库） */
    @EnumValue
    private final Integer value;
    /** 状态描述 */
    private final String description;

    /**
     * 根据状态值获取枚举
     *
     * @param value 状态值
     * @return 对应的状态枚举，未找到返回null
     */
    public static OrderStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (OrderStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
