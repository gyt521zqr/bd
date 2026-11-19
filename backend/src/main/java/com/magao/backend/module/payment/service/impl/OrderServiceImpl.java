package com.magao.backend.module.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.config.PaymentProperties;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.exception.PaymentException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.course.entity.Course;
import com.magao.backend.module.course.mapper.CourseMapper;
import com.magao.backend.module.growth.entity.PointGoods;
import com.magao.backend.module.growth.mapper.PointGoodsMapper;
import com.magao.backend.module.marketing.entity.Coupon;
import com.magao.backend.module.marketing.entity.Membership;
import com.magao.backend.module.marketing.entity.UserCoupon;
import com.magao.backend.module.marketing.mapper.CouponMapper;
import com.magao.backend.module.marketing.mapper.MembershipMapper;
import com.magao.backend.module.marketing.mapper.UserCouponMapper;
import com.magao.backend.module.payment.entity.PayOrder;
import com.magao.backend.module.payment.entity.PayRefund;
import com.magao.backend.module.payment.event.OrderPaidEvent;
import com.magao.backend.module.payment.gateway.PaymentGateway;
import com.magao.backend.module.payment.mapper.PayOrderMapper;
import com.magao.backend.module.payment.mapper.PayRefundMapper;
import com.magao.backend.module.payment.service.OrderService;
import com.magao.backend.module.payment.vo.OrderItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现类（正式收费 P0-01~P0-04 重构）
 * <p>
 * 核心变更：
 * 1. 金额一律查库（course_info / membership / point_goods），不再写死；商品下架拒绝下单；
 * 2. 优惠券真实校验归属/门槛/有效期并核销；
 * 3. payOrder 只向网关下单拿支付凭证，绝不直接标记已支付；
 * 4. 只有 confirmPaid（回调验签后 / 主动查单确认）才置已支付，带幂等 + 金额核对；
 * 5. 退款走真实网关，失败抛异常。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements OrderService {

    private final PayRefundMapper payRefundMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final List<PaymentGateway> paymentGateways;
    private final PaymentProperties paymentProperties;
    private final CourseMapper courseMapper;
    private final MembershipMapper membershipMapper;
    private final PointGoodsMapper pointGoodsMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public PayOrder createOrder(Long userId, Integer productType, Long productId, Integer payChannel) {
        PayOrder order = new PayOrder();
        order.setOrderNo(newOrderNo());
        order.setUserId(userId);
        order.setProductType(productType);
        order.setProductId(productId);
        order.setPayChannel(payChannel);
        order.setStatus(0);
        order.setExpireTime(LocalDateTime.now().plusMinutes(paymentProperties.getOrderExpireMinutes()));
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
    @Transactional(rollbackFor = Exception.class)
    public OrderItemVO createOrderFrontend(Long userId, String productType, Long productId, Long couponId) {
        int pt = mapProductType(productType);

        // P0-03 查库定价：按商品 ID 读取真实价格与标题，商品不存在/下架则拒绝下单
        ProductSnapshot product = resolveProduct(pt, productId);

        PayOrder order = new PayOrder();
        order.setOrderNo(newOrderNo());
        order.setUserId(userId);
        order.setProductType(pt);
        order.setProductId(productId);
        order.setProductName(product.name);
        order.setAmount(product.price);
        order.setPayChannel(1);
        order.setStatus(0);
        order.setExpireTime(LocalDateTime.now().plusMinutes(paymentProperties.getOrderExpireMinutes()));

        BigDecimal payAmount = product.price;
        if (couponId != null) {
            payAmount = applyCoupon(userId, couponId, product.price);
            order.setCouponId(couponId);
        }
        order.setPayAmount(payAmount);
        save(order);

        // 优惠券在下单成功时锁定，支付回调确认后核销（见 confirmPaid）
        if (couponId != null) {
            lockCoupon(userId, couponId, order.getId());
        }
        log.info("订单创建成功：orderNo={}, userId={}, amount={}, payAmount={}",
                order.getOrderNo(), userId, product.price, payAmount);
        return toItemVO(order);
    }

    /**
     * 商品快照（名称 + 真实价格），禁止使用前端传入的价格
     */
    private ProductSnapshot resolveProduct(int productType, Long productId) {
        if (productId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商品ID不能为空");
        }
        switch (productType) {
            case 1: { // 课程
                Course c = courseMapper.selectById(productId);
                if (c == null || c.getStatus() == null || c.getStatus() != 1) {
                    throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "课程不存在或已下架");
                }
                return new ProductSnapshot(c.getTitle(), priceOf(c.getPrice()));
            }
            case 2: { // 会员套餐
                Membership m = membershipMapper.selectById(productId);
                if (m == null || m.getStatus() == null || m.getStatus() != 1) {
                    throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "会员套餐不存在或已下架");
                }
                return new ProductSnapshot(m.getMembershipName(), priceOf(m.getPrice()));
            }
            case 3: { // 积分商品
                PointGoods g = pointGoodsMapper.selectById(productId);
                if (g == null || g.getStatus() == null || g.getStatus() != 1 || (g.getStock() != null && g.getStock() <= 0)) {
                    throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "积分商品不存在或已售罄");
                }
                // 积分商品用 original_price 作为现金购买价；若为 0 表示不支持现金购买
                BigDecimal cashPrice = priceOf(g.getOriginalPrice());
                if (cashPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该积分商品不支持现金购买");
                }
                return new ProductSnapshot(g.getGoodsName(), cashPrice);
            }
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "商品类型不合法");
        }
    }

    private BigDecimal priceOf(BigDecimal p) {
        if (p == null || p.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "商品价格异常");
        }
        return p.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算优惠券折扣，返回应付金额
     */
    private BigDecimal applyCoupon(Long userId, Long couponId, BigDecimal orderAmount) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || (coupon.getStatus() != null && coupon.getStatus() != 1)) {
            throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "优惠券不可用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidStartTime() != null && now.isBefore(coupon.getValidStartTime())
                || coupon.getValidEndTime() != null && now.isAfter(coupon.getValidEndTime())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "优惠券不在有效期内");
        }
        if (coupon.getMinAmount() != null && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "订单金额未达到优惠券使用门槛");
        }
        // 归属校验：该券必须属于当前用户且未使用
        UserCoupon uc = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getStatus, 0)
                .last("LIMIT 1"));
        if (uc == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "优惠券不存在或已使用");
        }
        BigDecimal discount;
        if (coupon.getCouponType() == 2) { // 折扣券：discountValue 为折扣率，如 0.8
            BigDecimal rate = coupon.getDiscountValue();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw new BusinessException(ErrorCode.DATA_STATUS_ERROR, "优惠券折扣配置异常");
            }
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(rate));
        } else { // 满减/无门槛券
            discount = coupon.getDiscountValue() == null ? BigDecimal.ZERO : coupon.getDiscountValue();
        }
        BigDecimal pay = orderAmount.subtract(discount).setScale(2, BigDecimal.ROUND_HALF_UP);
        return pay.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2) : pay;
    }

    /** 下单时锁定优惠券（status 0->3 锁定中），支付确认后核销（status 3->1） */
    private void lockCoupon(Long userId, Long couponId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getStatus, 0)
                .last("LIMIT 1"));
        if (uc == null) return;
        uc.setStatus(3);
        uc.setOrderId(orderId);
        userCouponMapper.updateById(uc);
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
        releaseCoupon(order);
        log.info("订单取消：orderId={}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId, Long userId, String reason) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        if (!order.getUserId().equals(userId)) { throw new PaymentException(ErrorCode.FORBIDDEN); }
        if (order.getStatus() != 1) { throw new PaymentException(ErrorCode.OPERATION_NOT_ALLOWED); }

        String refundNo = "R" + UUID.randomUUID().toString().replace("-", "").substring(0, 19);
        // P0-04 调真实网关退款；失败抛异常，不落"退款成功"记录
        PaymentGateway gateway = gatewayFor(order.getPayChannel());
        String thirdPartyRefundNo = gateway.refund(order, refundNo, order.getPayAmount());

        order.setStatus(4);
        updateById(order);

        PayRefund refund = new PayRefund();
        refund.setRefundNo(refundNo);
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setUserId(userId);
        refund.setRefundAmount(order.getPayAmount());
        refund.setReason(reason);
        refund.setStatus(1);
        refund.setRefundTime(LocalDateTime.now());
        refund.setTradeNo(thirdPartyRefundNo);
        payRefundMapper.insert(refund);
        log.info("订单退款成功：orderId={}, refundNo={}, thirdNo={}", orderId, refundNo, thirdPartyRefundNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> payOrder(Long orderId, String payMethod) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        if (order.getStatus() != 0) { throw new PaymentException(ErrorCode.OPERATION_NOT_ALLOWED); }
        if (order.getExpireTime() != null && LocalDateTime.now().isAfter(order.getExpireTime())) {
            order.setStatus(5);
            updateById(order);
            releaseCoupon(order);
            throw new PaymentException(ErrorCode.ORDER_EXPIRED);
        }
        int channel = mapPayMethod(payMethod);
        order.setPayChannel(channel);
        updateById(order);

        // P0-01：只向网关下单拿支付凭证，绝不直接置为已支付
        PaymentGateway gateway = gatewayFor(channel);
        Map<String, String> result = gateway.createPayment(order);
        // 带上订单标识，前端据此轮询状态 / demo 模式触发模拟支付
        result.put("orderNo", order.getOrderNo());
        result.put("orderId", String.valueOf(order.getId()));
        result.put("demo", String.valueOf(paymentProperties.isDemo()));
        log.info("支付下单成功（待回调）：orderNo={}, channel={}", order.getOrderNo(), channel);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPaid(String orderNo, String tradeNo, BigDecimal paidAmount) {
        PayOrder order = getOrderByNo(orderNo);
        // 幂等：已支付直接返回，不重复开通
        if (order.getStatus() != null && order.getStatus() >= 1) {
            log.info("订单已确认，幂等跳过：orderNo={}, status={}", orderNo, order.getStatus());
            return false;
        }
        // 金额核对：防止小额支付刷单
        if (paidAmount != null && order.getPayAmount() != null
                && paidAmount.compareTo(order.getPayAmount()) != 0) {
            log.error("回调金额不符，拒绝确认：orderNo={}, expect={}, actual={}",
                    orderNo, order.getPayAmount(), paidAmount);
            throw new BusinessException(ErrorCode.PAYMENT_ERROR, "支付金额与订单不符");
        }
        order.setStatus(1);
        order.setTradeNo(tradeNo);
        order.setPayTime(LocalDateTime.now());
        updateById(order);

        // 核销优惠券（锁定 -> 已使用）
        if (order.getCouponId() != null) {
            UserCoupon uc = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getId, order.getCouponId())
                    .eq(UserCoupon::getStatus, 3));
            if (uc != null) {
                uc.setStatus(1);
                uc.setUseTime(LocalDateTime.now());
                userCouponMapper.updateById(uc);
            }
        }

        eventPublisher.publishEvent(new OrderPaidEvent(
                order.getId(), order.getOrderNo(), order.getUserId(),
                order.getProductType(), order.getProductId(),
                order.getPayAmount(), order.getPayTime()));
        log.info("订单支付已确认：orderNo={}, tradeNo={}", orderNo, tradeNo);
        return true;
    }

    @Override
    public Map<String, Object> getStatus(Long orderId) {
        PayOrder order = getById(orderId);
        if (order == null) { throw new PaymentException(ErrorCode.ORDER_NOT_FOUND); }
        Map<String, Object> m = new HashMap<>();
        m.put("status", mapStatus(order.getStatus()));
        m.put("payTime", order.getPayTime() == null ? null : order.getPayTime().toString());

        // P0-02 查单补偿：待支付订单主动向网关查询，防止回调丢失导致"付了钱没开课"
        if (order.getStatus() == 0) {
            try {
                String tradeNo = gatewayFor(order.getPayChannel()).queryPaidTradeNo(order);
                if (tradeNo != null) {
                    confirmPaid(order.getOrderNo(), tradeNo, order.getPayAmount());
                    m.put("status", "paid");
                    m.put("payTime", LocalDateTime.now().toString());
                }
            } catch (PaymentException ignored) {
                // demo 模式查单不支持自动确认，忽略
            }
        }
        return m;
    }

    /** 释放订单锁定的优惠券（取消/过期时） */
    private void releaseCoupon(PayOrder order) {
        if (order.getCouponId() == null) return;
        UserCoupon uc = userCouponMapper.selectById(order.getCouponId());
        if (uc != null && uc.getStatus() != null && uc.getStatus() == 3) {
            uc.setStatus(0);
            uc.setOrderId(null);
            userCouponMapper.updateById(uc);
        }
    }

    private PaymentGateway gatewayFor(int channel) {
        return paymentGateways.stream()
                .filter(g -> g.channelCode() == channel)
                .min(Comparator.comparingInt(g -> g.channelCode()))
                .orElseThrow(() -> new BusinessException(ErrorCode.SERVICE_UNAVAILABLE, "支付渠道不可用"));
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

    private String newOrderNo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
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
        // 演示模式只有 demo 网关（channel 3）被加载；把该模式下的所有请求都路由到它，
        // 避免前端选微信/支付宝时 gatewayFor(1/2) 找不到 bean 抛"渠道不可用"。
        if (paymentProperties.isDemo()) return 3;
        if (payMethod == null) return 1;
        switch (payMethod) {
            case "alipay": return 2;
            case "wechat": return 1;
            default: throw new BusinessException(ErrorCode.PAYMENT_ERROR, "不支持的支付方式");
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

    @Override
    public void assertOwned(Long orderId, Long userId) {
        PayOrder order = orderId == null ? null : getById(orderId);
        if (order == null) throw new PaymentException(ErrorCode.ORDER_NOT_FOUND);
        if (userId == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该订单");
        }
    }

    @Override
    public boolean closeExpiredOrder(String orderNo) {
        PayOrder order = getOrderByNo(orderNo);
        // 幂等：仅待支付(0)才关单；已支付/已取消/已退款一律跳过
        if (order.getStatus() == null || order.getStatus() != 0) {
            log.info("超时关单幂等跳过：orderNo={}, status={}", orderNo, order.getStatus());
            return false;
        }
        order.setStatus(5); // 已过期
        updateById(order);
        releaseCoupon(order);
        log.info("订单超时已关闭：orderNo={}", orderNo);
        return true;
    }

    private static class ProductSnapshot {
        final String name;
        final BigDecimal price;
        ProductSnapshot(String name, BigDecimal price) { this.name = name; this.price = price; }
    }
}
