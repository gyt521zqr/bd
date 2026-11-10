package com.magao.backend.module.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.PaymentException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.payment.entity.PayOrder;
import com.magao.backend.module.payment.entity.PayRefund;
import com.magao.backend.module.payment.mapper.PayOrderMapper;
import com.magao.backend.module.payment.mapper.PayRefundMapper;
import com.magao.backend.module.payment.service.OrderService;
import com.magao.backend.module.payment.event.OrderPaidEvent;
import com.magao.backend.module.payment.vo.OrderItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements OrderService {

    private final PayRefundMapper payRefundMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PayOrder createOrder(Long userId, Integer productType, Long productId, Integer payChannel) {
        PayOrder order = new PayOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setUserId(userId);
        order.setProductType(productType);
        order.setProductId(productId);
        order.setPayChannel(payChannel);
        order.setStatus(0);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        save(order);
        log.info("订单创建成功：orderNo={}, userId={}", order.getOrderNo(), userId);
        return order;
    }

    @Override
    public PayOrder getOrderByNo(String orderNo) {
        PayOrder order = getOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderNo, orderNo));
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        return order;
    }

    @Override
    public Page<PayOrder> pageList(PageDTO pageDTO, Long userId, Integer status) {
        Page<PayOrder> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) { wrapper.eq(PayOrder::getUserId, userId); }
        if (status != null) { wrapper.eq(PayOrder::getStatus, status); }
        wrapper.orderByDesc(PayOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public OrderItemVO createOrderFrontend(Long userId, String productType, Long productId, Long couponId) {
        int pt = mapProductType(productType);
        PayOrder order = new PayOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setUserId(userId);
        order.setProductType(pt);
        order.setProductId(productId);
        order.setProductName(defaultName(pt, productId));
        BigDecimal amount = defaultAmount(pt);
        order.setAmount(amount);
        order.setPayAmount(amount);
        order.setPayChannel(1);
        order.setStatus(0);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        save(order);
        if (couponId != null) {
            log.info("订单使用优惠券：orderNo={}, couponId={}", order.getOrderNo(), couponId);
        }
        return toItemVO(order);
    }

    @Override
    public OrderItemVO getDetail(Long orderId) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        return toItemVO(order);
    }

    @Override
    public Page<OrderItemVO> pageListVO(PageDTO pageDTO, Long userId, Integer status) {
        Page<PayOrder> res = pageList(pageDTO, userId, status);
        Page<OrderItemVO> out = new Page<>(res.getCurrent(), res.getSize(), res.getTotal());
        List<OrderItemVO> list = res.getRecords().stream().map(this::toItemVO).collect(Collectors.toList());
        out.setRecords(list);
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        if (!order.getUserId().equals(userId)) { throw new PaymentException(ErrorCode.FORBIDDEN); }
        if (order.getStatus() != 0) { throw new PaymentException(ErrorCode.OPERATION_NOT_ALLOWED); }
        order.setStatus(2);
        updateById(order);
        log.info("订单取消：orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId, Long userId, String reason) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        if (!order.getUserId().equals(userId)) { throw new PaymentException(ErrorCode.FORBIDDEN); }
        if (order.getStatus() != 1) { throw new PaymentException(ErrorCode.OPERATION_NOT_ALLOWED); }
        order.setStatus(4);
        updateById(order);
        PayRefund refund = new PayRefund();
        refund.setRefundNo("R" + UUID.randomUUID().toString().replace("-", "").substring(0, 19));
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setUserId(userId);
        refund.setRefundAmount(order.getPayAmount());
        refund.setReason(reason);
        refund.setStatus(1);
        refund.setRefundTime(LocalDateTime.now());
        refund.setTradeNo("MOCK-" + order.getOrderNo());
        payRefundMapper.insert(refund);
        log.info("订单退款：orderId={}, refundNo={}", orderId, refund.getRefundNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> payOrder(Long orderId, String payMethod) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        if (order.getStatus() != 0) { throw new PaymentException(ErrorCode.OPERATION_NOT_ALLOWED); }
        int channel = mapPayMethod(payMethod);
        order.setPayChannel(channel);
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setTradeNo("MOCK-" + order.getOrderNo());
        updateById(order);
        // 发布支付成功事件：由业务模块（如课程入学）同步监听处理，保证支付后业务数据一致
        eventPublisher.publishEvent(new OrderPaidEvent(
                order.getId(), order.getOrderNo(), order.getUserId(),
                order.getProductType(), order.getProductId(),
                order.getPayAmount(), order.getPayTime()));
        Map<String, String> m = new HashMap<>();
        m.put("payUrl", "/api/payment/mock-pay?orderNo=" + order.getOrderNo());
        m.put("qrCode", "/api/payment/mock-qr?orderNo=" + order.getOrderNo());
        log.info("（演示）订单支付成功：orderNo={}, channel={}", order.getOrderNo(), channel);
        return m;
    }

    @Override
    public Map<String, Object> getStatus(Long orderId) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        Map<String, Object> m = new HashMap<>();
        m.put("status", mapStatus(order.getStatus()));
        m.put("payTime", order.getPayTime() == null ? null : order.getPayTime().toString());
        return m;
    }

    @Override
    public OrderItemVO toItemVO(PayOrder o) {
        OrderItemVO v = new OrderItemVO();
        v.setId(o.getId());
        v.setOrderNo(o.getOrderNo());
        v.setUserId(o.getUserId());
        v.setProductId(o.getProductId());
        v.setProductName(o.getProductName());
        v.setProductType(mapProductTypeStr(o.getProductType()));
        v.setPayAmount(o.getPayAmount() == null ? 0 : o.getPayAmount().doubleValue());
        v.setStatus(mapStatus(o.getStatus()));
        v.setPayMethod(mapPayMethodStr(o.getPayChannel()));
        v.setPayTime(o.getPayTime() == null ? null : o.getPayTime().toString());
        v.setCreateTime(o.getCreateTime() == null ? null : o.getCreateTime().toString());
        return v;
    }

    private int mapProductType(String productType) {
        if (productType == null) return 1;
        switch (productType) {
            case "membership": return 2;
            case "goods": return 3;
            case "course":
            default: return 1;
        }
    }

    private String mapProductTypeStr(Integer t) {
        if (t == null) return "course";
        switch (t) {
            case 2: return "membership";
            case 3: return "goods";
            default: return "course";
        }
    }

    private String mapStatus(Integer s) {
        if (s == null) return "pending";
        switch (s) {
            case 1: return "paid";
            case 2: return "cancelled";
            case 3:
            case 4: return "refunded";
            case 5: return "expired";
            default: return "pending";
        }
    }

    private int mapPayMethod(String payMethod) {
        if (payMethod == null) return 1;
        switch (payMethod) {
            case "alipay": return 2;
            case "balance": return 3;
            case "wechat":
            default: return 1;
        }
    }

    private String mapPayMethodStr(Integer c) {
        if (c == null) return "wechat";
        switch (c) {
            case 2: return "alipay";
            case 3:
            case 4: return "balance";
            default: return "wechat";
        }
    }

    private String defaultName(int productType, Long productId) {
        switch (productType) {
            case 2: return "会员套餐#" + productId;
            case 3: return "积分商品#" + productId;
            default: return "课程#" + productId;
        }
    }

    private BigDecimal defaultAmount(int productType) {
        switch (productType) {
            case 2: return new BigDecimal("299.00");
            case 3: return new BigDecimal("9.99");
            default: return new BigDecimal("199.00");
        }
    }
}
