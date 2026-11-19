package com.magao.backend.module.payment.gateway;

import com.magao.backend.module.payment.entity.PayOrder;

import java.util.Map;

/**
 * 支付网关统一抽象（正式收费 P0-01/P0-02）
 * <p>
 * 所有支付渠道实现此接口，OrderService 只依赖抽象，不感知具体 SDK。
 * 渠道实现必须保证：
 * 1. createPayment 只返回支付凭证，绝不修改订单状态；
 * 2. 只有 notify 回调（验签通过后）或 verifyCallback 主动查单确认后才可置为已支付；
 * 3. refund 调真实网关，失败抛异常，绝不静默"退款成功"。
 * </p>
 */
public interface PaymentGateway {

    /** 渠道标识：1-微信 2-支付宝 3-演示 */
    int channelCode();

    /**
     * 下单，返回前端拉起支付所需参数。
     *
     * @return payUrl / qrCode / prepayId 等，由渠道决定
     */
    Map<String, String> createPayment(PayOrder order);

    /**
     * 主动查单（前端轮询 / 回调补偿用）。
     *
     * @return 已支付返回第三方交易号，未支付返回 null
     */
    String queryPaidTradeNo(PayOrder order);

    /**
     * 退款。
     *
     * @return 第三方退款单号
     */
    String refund(PayOrder order, String refundNo, java.math.BigDecimal amount);

    /**
     * 校验并解析异步回调。
     *
     * @param headers HTTP 头（微信验签需要）
     * @param body    原始请求体
     * @return 解析结果（订单号、第三方交易号、金额）
     * @throws RuntimeException 验签失败 / 金额不符
     */
    CallbackResult verifyCallback(Map<String, String> headers, String body);

    /** 回调解析结果 */
    class CallbackResult {
        private final String orderNo;
        private final String tradeNo;
        private final java.math.BigDecimal amount;
        private final boolean success;

        public CallbackResult(String orderNo, String tradeNo, java.math.BigDecimal amount, boolean success) {
            this.orderNo = orderNo;
            this.tradeNo = tradeNo;
            this.amount = amount;
            this.success = success;
        }

        public String getOrderNo() { return orderNo; }
        public String getTradeNo() { return tradeNo; }
        public java.math.BigDecimal getAmount() { return amount; }
        public boolean isSuccess() { return success; }
    }
}
