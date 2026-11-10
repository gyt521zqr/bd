package com.magao.backend.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.payment.service.OrderService;
import com.magao.backend.module.payment.vo.OrderItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付控制器（对齐前端 /payment/* 契约）
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "支付", description = "订单创建、支付、状态、取消、退款等接口")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController extends BaseController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/order/create")
    public Result<OrderItemVO> createOrder(@RequestBody CreateOrderReq req) {
        return Result.success(orderService.createOrderFrontend(
                getCurrentUserId(), req.getProductType(), req.getProductId(), req.getCouponId()));
    }

    @Operation(summary = "订单列表")
    @GetMapping("/order/list")
    public Result<Page<OrderItemVO>> orderList(PageDTO pageDTO,
                                                @RequestParam(required = false) String status) {
        Integer statusCode = mapStatus(status);
        return Result.success(orderService.pageListVO(pageDTO, getCurrentUserId(), statusCode));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/order/detail/{orderId}")
    public Result<OrderItemVO> orderDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getDetail(orderId));
    }

    @Operation(summary = "发起支付")
    @PostMapping("/pay")
    public Result<Map<String, String>> pay(@RequestBody PayReq req) {
        return Result.success(orderService.payOrder(req.getOrderId(), req.getPayMethod()));
    }

    @Operation(summary = "支付状态")
    @GetMapping("/status/{orderId}")
    public Result<Map<String, Object>> status(@PathVariable Long orderId) {
        return Result.success(orderService.getStatus(orderId));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/order/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId, getCurrentUserId());
        return Result.success();
    }

    @Operation(summary = "申请退款")
    @PostMapping("/order/{orderId}/refund")
    public Result<Void> refund(@PathVariable Long orderId, @RequestBody(required = false) RefundReq req) {
        orderService.refundOrder(orderId, getCurrentUserId(), req == null ? null : req.getReason());
        return Result.success();
    }

    private Integer mapStatus(String status) {
        if (status == null) return null;
        switch (status) {
            case "paid": return 1;
            case "cancelled": return 2;
            case "refunded": return 4;
            case "expired": return 5;
            case "pending":
            default: return 0;
        }
    }

    /** 创建订单请求体 */
    public static class CreateOrderReq {
        private String productType;
        private Long productId;
        private Long couponId;
        public String getProductType() { return productType; }
        public void setProductType(String productType) { this.productType = productType; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getCouponId() { return couponId; }
        public void setCouponId(Long couponId) { this.couponId = couponId; }
    }

    /** 支付请求体 */
    public static class PayReq {
        private Long orderId;
        private String payMethod;
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getPayMethod() { return payMethod; }
        public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    }

    /** 退款请求体 */
    public static class RefundReq {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
