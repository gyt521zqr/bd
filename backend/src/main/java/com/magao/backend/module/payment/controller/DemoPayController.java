package com.magao.backend.module.payment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.common.config.PaymentProperties;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.payment.entity.PayOrder;
import com.magao.backend.module.payment.mapper.PayOrderMapper;
import com.magao.backend.module.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * 演示支付确认端点（仅 payment.mode=demo 时注册，用于本地/联调）
 * <p>
 * 生产环境（wechat/alipay 模式）此 Controller 不存在，因此无法通过它白嫖。
 * 真实模式的支付确认只认验签回调。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/demo")
@ConditionalOnProperty(name = "payment.mode", havingValue = "demo", matchIfMissing = true)
@RequiredArgsConstructor
public class DemoPayController {

    private final PayOrderMapper payOrderMapper;
    private final OrderService orderService;
    private final PaymentProperties props;

    /** 演示：模拟用户完成付款（联调专用，生产不存在此接口） */
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestBody ConfirmReq req) {
        if (!props.isDemo()) {
            throw new IllegalStateException("演示支付端点仅 demo 模式可用");
        }
        PayOrder order = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderNo, req.getOrderNo()));
        if (order == null) {
            return Result.fail("订单不存在");
        }
        orderService.confirmPaid(order.getOrderNo(), "DEMO-" + order.getOrderNo(), order.getPayAmount());
        log.info("[演示模式] 模拟支付完成：orderNo={}", req.getOrderNo());
        return Result.success();
    }

    /** 前端据此判断是否为演示模式，展示"模拟支付"按钮 */
    @GetMapping("/mode")
    public Result<String> mode() {
        return Result.success(props.getMode());
    }

    public static class ConfirmReq {
        private String orderNo;
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    }
}
