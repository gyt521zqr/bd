package com.magao.backend.module.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.payment.entity.PayOrder;
import com.magao.backend.module.payment.vo.OrderItemVO;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface OrderService extends IService<PayOrder> {

    /** 创建订单 */
    PayOrder createOrder(Long userId, Integer productType, Long productId, Integer payChannel);

    /** 查询订单 */
    PayOrder getOrderByNo(String orderNo);

    /** 分页查询订单列表 */
    Page<PayOrder> pageList(PageDTO pageDTO, Long userId, Integer status);

    /** 前端创建订单（字符串商品类型），对齐 /payment/order/create */
    OrderItemVO createOrderFrontend(Long userId, String productType, Long productId, Long couponId);

    /** 订单详情，对齐 /payment/order/detail/{id} */
    OrderItemVO getDetail(Long orderId);

    /** 分页订单列表（转 VO），对齐 /payment/order/list */
    Page<OrderItemVO> pageListVO(PageDTO pageDTO, Long userId, Integer status);

    /** 取消订单，对齐 /payment/order/{id}/cancel */
    void cancelOrder(Long orderId, Long userId);

    /** 申请退款，对齐 /payment/order/{id}/refund */
    void refundOrder(Long orderId, Long userId, String reason);

    /** 发起支付，对齐 /payment/pay，返回 {payUrl, qrCode} */
    Map<String, String> payOrder(Long orderId, String payMethod);

    /** 支付状态，对齐 /payment/status/{id}，返回 {status, payTime} */
    Map<String, Object> getStatus(Long orderId);

    /** PayOrder -> OrderItemVO 枚举转换 */
    OrderItemVO toItemVO(PayOrder order);

    /**
     * 确认订单支付成功（P0-02）：仅供支付回调 / 主动查单补偿调用，带幂等。
     * 校验金额一致后置为已支付并发布开通事件；重复回调直接返回不重复开通。
     *
     * @param orderNo      订单号
     * @param tradeNo      第三方交易号
     * @param paidAmount   回调金额（用于核对，防止小额支付刷单）
     * @return 是否发生状态变更（true=本次确认成功，false=已确认过/幂等跳过）
     */
    boolean confirmPaid(String orderNo, String tradeNo, java.math.BigDecimal paidAmount);

    /**
     * 超时关单（MQ 消费者调用，幂等）：仅当订单仍为待支付时关闭并释放锁定的优惠券。
     *
     * @return 是否发生了状态变更
     */
    boolean closeExpiredOrder(String orderNo);

    /**
     * P0-14：校验订单归属，越权抛出 FORBIDDEN。
     * 供 detail/status/pay 等按 orderId 访问的接口调用。
     */
    void assertOwned(Long orderId, Long userId);
}
