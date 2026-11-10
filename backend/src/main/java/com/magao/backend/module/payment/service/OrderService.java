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
}
