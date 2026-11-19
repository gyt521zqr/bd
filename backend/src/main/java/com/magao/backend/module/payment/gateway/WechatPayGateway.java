package com.magao.backend.module.payment.gateway;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.AutoCertificateNotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.magao.backend.common.config.PaymentProperties;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.payment.entity.PayOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付 V3 Native（扫码）网关（正式收费 P0-01/P0-02）
 * <p>仅在 payment.mode=wechat 时激活。凭证缺失时快速失败，绝不降级为"假支付"。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "payment.mode", havingValue = "wechat")
@RequiredArgsConstructor
public class WechatPayGateway implements PaymentGateway {

    private final PaymentProperties props;

    private volatile NativePayService nativePayService;
    private volatile RefundService refundService;
    private volatile NotificationParser notificationParser;

    private RSAAutoCertificateConfig config() {
        validateConfig();
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(props.getWechatMchId())
                .privateKey(props.getWechatPrivateKey())
                .merchantSerialNumber(props.getWechatCertSerialNo())
                .apiV3Key(props.getWechatApiV3Key())
                .build();
    }

    private NativePayService payService() {
        if (nativePayService == null) {
            synchronized (this) {
                if (nativePayService == null) {
                    nativePayService = new NativePayService.Builder().config(config()).build();
                }
            }
        }
        return nativePayService;
    }

    private void validateConfig() {
        if (isBlank(props.getWechatMchId()) || isBlank(props.getWechatPrivateKey())
                || isBlank(props.getWechatCertSerialNo()) || isBlank(props.getWechatApiV3Key())) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "微信支付凭证未配置完整，请检查 payment.wechat-* 环境变量");
        }
    }

    @Override
    public int channelCode() { return 1; }

    @Override
    public Map<String, String> createPayment(PayOrder order) {
        PrepayRequest req = new PrepayRequest();
        req.setAppid(props.getWechatAppId());
        req.setMchid(props.getWechatMchId());
        req.setDescription(order.getProductName());
        req.setOutTradeNo(order.getOrderNo());
        req.setNotifyUrl(props.getNotifyBaseUrl().replaceAll("/$", "") + "/api/payment/notify/wechat");
        Amount amount = new Amount();
        amount.setTotal(order.getPayAmount().multiply(new BigDecimal("100")).intValue()); // 单位：分
        amount.setCurrency("CNY");
        req.setAmount(amount);

        PrepayResponse resp = payService().prepay(req);
        Map<String, String> out = new HashMap<>();
        out.put("qrCode", resp.getCodeUrl());
        out.put("payUrl", resp.getCodeUrl());
        return out;
    }

    @Override
    public String queryPaidTradeNo(PayOrder order) {
        QueryOrderByOutTradeNoRequest req = new QueryOrderByOutTradeNoRequest();
        req.setMchid(props.getWechatMchId());
        req.setOutTradeNo(order.getOrderNo());
        try {
            Transaction tx = payService().queryOrderByOutTradeNo(req);
            if (tx != null && tx.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                return tx.getTransactionId();
            }
        } catch (Exception e) {
            log.warn("微信查单失败：orderNo={}, err={}", order.getOrderNo(), e.getMessage());
        }
        return null;
    }

    @Override
    public String refund(PayOrder order, String refundNo, BigDecimal amount) {
        if (refundService == null) {
            synchronized (this) {
                if (refundService == null) {
                    refundService = new RefundService.Builder().config(config()).build();
                }
            }
        }
        CreateRequest req = new CreateRequest();
        req.setOutTradeNo(order.getOrderNo());
        req.setOutRefundNo(refundNo);
        req.setReason("用户申请退款");
        AmountReq amt = new AmountReq();
        amt.setRefund(amount.multiply(new BigDecimal("100")).longValue());
        amt.setTotal(order.getPayAmount().multiply(new BigDecimal("100")).longValue());
        amt.setCurrency("CNY");
        req.setAmount(amt);
        Refund resp = refundService.create(req);
        return resp.getRefundId();
    }

    @Override
    public CallbackResult verifyCallback(Map<String, String> headers, String body) {
        try {
            if (notificationParser == null) {
                synchronized (this) {
                    if (notificationParser == null) {
                        AutoCertificateNotificationConfig nc = new AutoCertificateNotificationConfig.Builder()
                                .merchantId(props.getWechatMchId())
                                .apiV3Key(props.getWechatApiV3Key())
                                .privateKey(props.getWechatPrivateKey())
                                .merchantSerialNumber(props.getWechatCertSerialNo())
                                .build();
                        notificationParser = new NotificationParser(nc);
                    }
                }
            }
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(headers.get("Wechatpay-Serial"))
                    .nonce(headers.get("Wechatpay-Nonce"))
                    .signature(headers.get("Wechatpay-Signature"))
                    .timestamp(headers.get("Wechatpay-Timestamp"))
                    .body(body)
                    .build();
            Transaction tx = notificationParser.parse(requestParam, Transaction.class);
            if (tx == null) {
                throw new BusinessException(ErrorCode.PAYMENT_ERROR, "微信回调解析失败");
            }
            BigDecimal paidAmount = new BigDecimal(tx.getAmount().getTotal())
                    .divide(new BigDecimal("100"));
            boolean success = tx.getTradeState() == Transaction.TradeStateEnum.SUCCESS;
            return new CallbackResult(tx.getOutTradeNo(), tx.getTransactionId(), paidAmount, success);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信回调验签失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_ERROR, "微信回调验签失败");
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
