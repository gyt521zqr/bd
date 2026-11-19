package com.magao.backend.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.payment.entity.PayOrder;
import com.magao.backend.module.payment.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "订单管理", description = "订单创建、查询等接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController extends BaseController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @LogOperation(module = "订单", type = "创建", description = "创建订单", recordParam = true)
    @PostMapping("/create")
    public Result<PayOrder> create(@RequestParam Integer productType, @RequestParam Long productId, @RequestParam Integer payChannel) {
        return Result.success(orderService.createOrder(getCurrentUserId(), productType, productId, payChannel));
    }

    @Operation(summary = "查询订单")
    @GetMapping("/query")
    public Result<PayOrder> query(@RequestParam String orderNo) {
        PayOrder order = orderService.getOrderByNo(orderNo);
        // P0-14：归属校验，不能凭订单号查到他人订单
        Long currentId = getCurrentUserId();
        if (currentId == null || !currentId.equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该订单");
        }
        return Result.success(order);
    }

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public Result<Page<PayOrder>> list(PageDTO pageDTO, @RequestParam(required = false) Integer status) {
        return Result.success(orderService.pageList(pageDTO, getCurrentUserId(), status));
    }
}
