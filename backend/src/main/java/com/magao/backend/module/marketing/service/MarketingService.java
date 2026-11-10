package com.magao.backend.module.marketing.service;

import com.magao.backend.module.marketing.entity.Coupon;
import com.magao.backend.module.marketing.entity.Membership;

/**
 * 营销服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface MarketingService {

    /** 获取会员列表 */
    java.util.List<Membership> listMemberships();

    /** 领取优惠券 */
    void receiveCoupon(Long userId, Long couponId);

    /** 获取用户优惠券列表 */
    java.util.List<Coupon> listUserCoupons(Long userId);

    /** 获取全部优惠券（后台/可用列表用） */
    java.util.List<Coupon> listAllCoupons();
}
