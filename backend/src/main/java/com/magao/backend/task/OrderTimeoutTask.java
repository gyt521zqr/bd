package com.magao.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.module.payment.entity.PayOrder;
import com.magao.backend.module.payment.mapper.PayOrderMapper;
import com.magao.backend.module.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时关单定时任务（P1-27）
 * <p>
 * 定期扫描 expireTime 已过且仍处于待支付(0)状态的订单，关闭并回滚优惠券。
 * 幂等由 {@link OrderService#closeExpiredOrder(String)} 保证。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;
    private final PayOrderMapper payOrderMapper;

    /** 每 60 秒扫描一次（首次延迟 30 秒启动） */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void closeTimeoutOrders() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getStatus, 0)
                .isNotNull(PayOrder::getExpireTime)
                .lt(PayOrder::getExpireTime, now)
                .last("LIMIT 200");
        List<PayOrder> expired = payOrderMapper.selectList(wrapper);
        if (expired.isEmpty()) return;

        int closed = 0;
        for (PayOrder order : expired) {
            try {
                if (orderService.closeExpiredOrder(order.getOrderNo())) {
                    closed++;
                }
            } catch (Exception e) {
                log.warn("超时关单失败：orderNo={}, error={}", order.getOrderNo(), e.getMessage());
            }
        }
        if (closed > 0) {
            log.info("超时关单完成：本次关闭 {} 笔，扫描 {} 笔", closed, expired.size());
        }
    }
}
