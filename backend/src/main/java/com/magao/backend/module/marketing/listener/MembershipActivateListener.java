package com.magao.backend.module.marketing.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.module.marketing.entity.Membership;
import com.magao.backend.module.marketing.entity.UserMembership;
import com.magao.backend.module.marketing.mapper.MembershipMapper;
import com.magao.backend.module.marketing.mapper.UserMembershipMapper;
import com.magao.backend.module.payment.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 会员开通监听器（P0-04：修复"买会员不生效"）
 * <p>
 * 监听 OrderPaidEvent，productType=2（会员）时按套餐有效期开通/续期会员权益。
 * 幂等：uk_user_plan 唯一索引 + orderId 去重，避免重复回调重复开通。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipActivateListener {

    private final UserMembershipMapper userMembershipMapper;
    private final MembershipMapper membershipMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        if (event.getProductType() == null || event.getProductType() != 2) {
            return;
        }
        Long userId = event.getUserId();
        Long planId = event.getProductId();

        Membership plan = membershipMapper.selectById(planId);
        if (plan == null || plan.getValidDays() == null || plan.getValidDays() <= 0) {
            log.error("会员套餐有效期配置异常，无法开通：planId={}, orderId={}", planId, event.getOrderId());
            return;
        }

        // 幂等：同一订单重复回调不重复开通
        UserMembership existByOrder = userMembershipMapper.selectOne(
                new LambdaQueryWrapper<UserMembership>().eq(UserMembership::getOrderId, event.getOrderId()));
        if (existByOrder != null) {
            log.info("会员订单已开通，幂等跳过：orderId={}", event.getOrderId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // 查询该用户该套餐已有权益，若在有效期内则顺延，否则从现在开始
        UserMembership existing = userMembershipMapper.selectOne(
                new LambdaQueryWrapper<UserMembership>()
                        .eq(UserMembership::getUserId, userId)
                        .eq(UserMembership::getPlanId, planId));

        if (existing == null) {
            UserMembership um = new UserMembership();
            um.setUserId(userId);
            um.setPlanId(planId);
            um.setOrderId(event.getOrderId());
            um.setStartTime(now);
            um.setExpireTime(now.plusDays(plan.getValidDays()));
            um.setStatus(1);
            userMembershipMapper.insert(um);
        } else {
            LocalDateTime base = existing.getExpireTime() != null && existing.getExpireTime().isAfter(now)
                    ? existing.getExpireTime() : now;
            existing.setOrderId(event.getOrderId());
            existing.setStartTime(now);
            existing.setExpireTime(base.plusDays(plan.getValidDays()));
            existing.setStatus(1);
            userMembershipMapper.updateById(existing);
        }
        log.info("会员权益开通成功：userId={}, planId={}, orderId={}, validDays={}",
                userId, planId, event.getOrderId(), plan.getValidDays());
    }
}
