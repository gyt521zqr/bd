package com.magao.backend.module.payment.controller;

import com.magao.backend.module.payment.entity.PayCallbackLog;
import com.magao.backend.module.payment.entity.PayOrder;
import com.magao.backend.module.payment.gateway.PaymentGateway;
import com.magao.backend.module.payment.mapper.PayCallbackLogMapper;
import com.magao.backend.module.payment.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付异步回调控制器（正式收费 P0-02）
 * <p>
 * 安全设计：
 * 1. 回调路径在 JwtAuthFilter 白名单（支付机构不携带本站 JWT），安全性由网关验签保证；
 * 2. 先验签、再核对金额、最后 confirmPaid（幂等 + 金额二次核对）；
 * 3. 每次回调（成功/失败）都落 pay_callback_log，可审计、可追溯。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/notify")
@RequiredArgsConstructor
public class PaymentNotifyController {

    private final List<PaymentGateway> paymentGateways;
    private final OrderService orderService;
    private final PayCallbackLogMapper callbackLogMapper;

    /** 微信支付 V3 回调 */
    @PostMapping(value = "/wechat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> wechat(HttpServletRequest request, @RequestBody String body) {
        return handle(1, headers(request), body, "wechat");
    }

    /** 支付宝回调 */
    @PostMapping(value = "/alipay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> alipay(HttpServletRequest request,
                                                        @RequestParam Map<String, String> params) {
        // 支付宝回调以表单参数提供，重新序列化为 form body 供网关验签
        String body = toFormBody(params);
        return handle(2, headers(request), body, "alipay");
    }

    private ResponseEntity<Map<String, Object>> handle(int channel, Map<String, String> headers,
                                                        String body, String channelName) {
        PayCallbackLog logRow = new PayCallbackLog();
        logRow.setPayChannel(channel);
        logRow.setCallbackData(truncate(body, 2000));
        logRow.setCreateTime(LocalDateTime.now());
        try {
            PaymentGateway gateway = paymentGateways.stream()
                    .filter(g -> g.channelCode() == channel)
                    .min(Comparator.comparingInt(PaymentGateway::channelCode))
                    .orElseThrow(() -> new IllegalStateException("支付渠道未启用：" + channelName));

            PaymentGateway.CallbackResult result = gateway.verifyCallback(headers, body);
            if (result == null || !result.isSuccess()) {
                logRow.setStatus(0);
                logRow.setErrorMsg("回调非成功状态");
                callbackLogMapper.insert(logRow);
                return ok(channelName, "FAIL"); // 通知网关不重试非成功交易
            }
            logRow.setOrderNo(result.getOrderNo());

            // confirmPaid 内部做幂等 + 金额核对
            boolean changed = orderService.confirmPaid(result.getOrderNo(), result.getTradeNo(), result.getAmount());
            logRow.setStatus(1);
            callbackLogMapper.insert(logRow);
            log.info("支付回调处理成功：channel={}, orderNo={}, 状态变更={}", channelName, result.getOrderNo(), changed);
            return ok(channelName, "SUCCESS");
        } catch (Exception e) {
            logRow.setStatus(0);
            logRow.setErrorMsg(truncate(e.getMessage(), 500));
            callbackLogMapper.insert(logRow);
            log.error("支付回调处理失败：channel={}, err={}", channelName, e.getMessage());
            // 验签/系统异常返回失败，让网关重试
            return ok(channelName, "FAIL");
        }
    }

    private ResponseEntity<Map<String, Object>> ok(String channelName, String result) {
        Map<String, Object> m = new HashMap<>();
        if ("alipay".equals(channelName)) {
            m.put("success", "SUCCESS".equals(result));
        } else {
            m.put("code", "SUCCESS".equals(result) ? "SUCCESS" : "FAIL");
            m.put("message", "success");
        }
        return ResponseEntity.ok(m);
    }

    private Map<String, String> headers(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            map.put(n, request.getHeader(n));
        }
        return map;
    }

    private String toFormBody(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (sb.length() > 0) sb.append('&');
            sb.append(k).append('=').append(v == null ? "" : v);
        });
        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
