package com.magao.backend.module.payment.gateway;

import com.magao.backend.module.payment.entity.PayOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示/联调支付网关（P0-01）
 * <p>
 * 仅在 payment.mode=demo（默认 dev）时激活，生产必须切到 wechat/alipay。
 * 关键约束：createPayment 只返回演示二维码，绝不修改订单状态；
 * "支付成功"只能由 DemoPayController 的受控回调触发，
 * 且该 Controller 同样仅在 demo 模式注册，避免生产环境被白嫖。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "payment.mode", havingValue = "demo", matchIfMissing = true)
public class DemoPaymentGateway implements PaymentGateway {

    @Override
    public int channelCode() { return 3; }

    @Override
    public Map<String, String> createPayment(PayOrder order) {
        // 演示二维码：指向受控演示回调页，二维码内容不再是写死的 "pay"
        String demoUrl = "/demo-pay.html?orderNo=" + order.getOrderNo()
                + "&amount=" + order.getPayAmount().toPlainString();
        Map<String, String> out = new HashMap<>();
        out.put("qrCode", demoUrl);
        out.put("payUrl", demoUrl);
        out.put("demo", "true");
        log.info("[演示模式] 生成支付二维码：orderNo={}", order.getOrderNo());
        return out;
    }

    @Override
    public String queryPaidTradeNo(PayOrder order) {
        // 演示模式主动查单不自动确认，必须走受控回调
        return null;
    }

    @Override
    public String refund(PayOrder order, String refundNo, BigDecimal amount) {
        // 演示模式下退款记录 DEMO 单号，但生产走真实网关
        return "DEMO-" + refundNo;
    }

    @Override
    public CallbackResult verifyCallback(Map<String, String> headers, String body) {
        // 演示回调由 DemoPayController 直接构造，此处不参与验签
        String orderNo = body;
        return new CallbackResult(orderNo, "DEMO-" + orderNo, null, true);
    }
}
