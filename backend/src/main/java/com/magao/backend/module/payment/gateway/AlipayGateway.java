package com.magao.backend.module.payment.gateway;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.magao.backend.common.config.PaymentProperties;
import com.magao.backend.common.exception.PaymentException;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.payment.entity.PayOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝当面付（预下单扫码）网关（正式收费 P0-01/P0-02）
 * <p>仅在 payment.mode=alipay 时激活。凭证缺失时快速失败。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "payment.mode", havingValue = "alipay")
@RequiredArgsConstructor
public class AlipayGateway implements PaymentGateway {

    private final PaymentProperties props;

    private void ensureInit() {
        validateConfig();
        Config config = new Config();
        config.appId = props.getAlipayAppId();
        config.merchantPrivateKey = props.getAlipayPrivateKey();
        config.alipayPublicKey = props.getAlipayPublicKey();
        config.signType = props.getAlipaySignType();
        config.gatewayHost = "openapi.alipay.com";
        config.protocol = "https";
        Factory.setOptions(config);
    }

    private void validateConfig() {
        if (isBlank(props.getAlipayAppId()) || isBlank(props.getAlipayPrivateKey()) || isBlank(props.getAlipayPublicKey())) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "支付宝凭证未配置完整，请检查 payment.alipay-* 环境变量");
        }
    }

    @Override
    public int channelCode() { return 2; }

    @Override
    public Map<String, String> createPayment(PayOrder order) {
        ensureInit();
        try {
            AlipayTradePrecreateResponse resp = Factory.Payment.FaceToFace().preCreate(
                    order.getProductName(), order.getOrderNo(), order.getPayAmount().toPlainString());
            if (resp == null || !"10000".equals(resp.code)) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR,
                        resp == null ? "支付宝下单无响应" : "支付宝下单失败：" + resp.subMsg);
            }
            Map<String, String> out = new HashMap<>();
            out.put("qrCode", resp.qrCode);
            out.put("payUrl", resp.qrCode);
            return out;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝下单异常：orderNo={}, err={}", order.getOrderNo(), e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "支付宝下单异常");
        }
    }

    @Override
    public String queryPaidTradeNo(PayOrder order) {
        ensureInit();
        try {
            AlipayTradeQueryResponse resp = Factory.Payment.Common().query(order.getOrderNo());
            if (resp != null && "10000".equals(resp.code)
                    && ("TRADE_SUCCESS".equals(resp.tradeStatus) || "TRADE_FINISHED".equals(resp.tradeStatus))) {
                return resp.tradeNo;
            }
        } catch (Exception e) {
            log.warn("支付宝查单失败：orderNo={}, err={}", order.getOrderNo(), e.getMessage());
        }
        return null;
    }

    @Override
    public String refund(PayOrder order, String refundNo, BigDecimal amount) {
        ensureInit();
        try {
            // EasySDK refund(outTradeNo, outRefundNo) 为全额退款；部分退款需改用 openapi 通用调用
            AlipayTradeRefundResponse resp = Factory.Payment.Common().refund(order.getOrderNo(), refundNo);
            if (resp == null || !"10000".equals(resp.code)) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "支付宝退款失败");
            }
            return resp.tradeNo;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝退款异常：orderNo={}, err={}", order.getOrderNo(), e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "支付宝退款异常");
        }
    }

    @Override
    public CallbackResult verifyCallback(Map<String, String> headers, String body) {
        ensureInit();
        try {
            Map<String, String> params = parseFormBody(body);
            boolean ok = Factory.Payment.Common().verifyNotify(params);
            if (!ok) {
                throw new BusinessException(ErrorCode.PAYMENT_ERROR, "支付宝回调验签失败");
            }
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String total = params.get("total_amount");
            BigDecimal amount = total == null ? null : new BigDecimal(total);
            boolean success = "TRADE_SUCCESS".equals(params.get("trade_status"))
                    || "TRADE_FINISHED".equals(params.get("trade_status"));
            return new CallbackResult(orderNo, tradeNo, amount, success);
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝回调解析异常：{}", e.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_ERROR, "支付宝回调解析异常");
        }
    }

    /** 解析 application/x-www-form-urlencoded 回调体 */
    private Map<String, String> parseFormBody(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        for (String pair : body.split("&")) {
            int i = pair.indexOf('=');
            if (i < 0) continue;
            String k = URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8);
            String v = URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8);
            map.put(k, v);
        }
        return map;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
