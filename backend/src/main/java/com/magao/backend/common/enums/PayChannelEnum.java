package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道枚举
 * <p>
 * 定义系统支持的支付方式：
 * - WECHAT：微信支付
 * - ALIPAY：支付宝
 * - BALANCE：余额支付
 * - COUPON：优惠券支付
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    /** 微信支付 */
    WECHAT(1, "微信支付"),
    /** 支付宝 */
    ALIPAY(2, "支付宝"),
    /** 余额支付 */
    BALANCE(3, "余额支付"),
    /** 优惠券 */
    COUPON(4, "优惠券");

    /** 渠道值（存储到数据库） */
    @EnumValue
    private final Integer value;
    /** 渠道描述 */
    private final String description;

    /**
     * 根据渠道值获取枚举
     *
     * @param value 渠道值
     * @return 对应的渠道枚举，未找到返回null
     */
    public static PayChannelEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PayChannelEnum channel : values()) {
            if (channel.getValue().equals(value)) {
                return channel;
            }
        }
        return null;
    }
}
