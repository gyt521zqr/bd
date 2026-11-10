package com.magao.backend.module.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.marketing.entity.Coupon;
import com.magao.backend.module.marketing.entity.Membership;
import com.magao.backend.module.marketing.entity.UserCoupon;
import com.magao.backend.module.marketing.mapper.CouponMapper;
import com.magao.backend.module.marketing.mapper.MembershipMapper;
import com.magao.backend.module.marketing.mapper.UserCouponMapper;
import com.magao.backend.module.marketing.service.MarketingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 营销服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingServiceImpl implements MarketingService {

    private final MembershipMapper membershipMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<Membership> listMemberships() {
        return membershipMapper.selectList(new LambdaQueryWrapper<Membership>().eq(Membership::getStatus, 1));
    }

    @Override
    public void receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0);
        userCouponMapper.insert(userCoupon);
        log.info("领取优惠券：userId={}, couponId={}", userId, couponId);
    }

    @Override
    public List<Coupon> listUserCoupons(Long userId) {
        // 查询该用户已领取的优惠券记录
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);

        if (userCoupons.isEmpty()) {
            return List.of();
        }

        // 提取couponId列表，批量查询优惠券详情
        List<Long> couponIds = userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toList());
        return couponMapper.selectBatchIds(couponIds);
    }

    @Override
    public List<Coupon> listAllCoupons() {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 1));
    }
}
