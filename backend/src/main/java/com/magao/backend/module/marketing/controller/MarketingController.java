package com.magao.backend.module.marketing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.marketing.entity.Coupon;
import com.magao.backend.module.marketing.entity.Membership;
import com.magao.backend.module.marketing.service.MarketingService;
import com.magao.backend.module.payment.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 营销管理控制器
 * <p>
 * 提供营销相关的API接口：会员列表、优惠券领取与查询
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "营销管理", description = "会员、优惠券等相关接口")
@RestController
@RequestMapping("/api/marketing")
@RequiredArgsConstructor
public class MarketingController extends BaseController {

    private final MarketingService marketingService;
    private final OrderService orderService;

    @Operation(summary = "会员列表")
    @GetMapping("/memberships")
    public Result<List<Membership>> listMemberships() {
        return Result.success(marketingService.listMemberships());
    }

    @Operation(summary = "会员套餐列表（对齐前端 /membership/plans）")
    @GetMapping("/membership/plans")
    public Result<List<Map<String, Object>>> membershipPlans() {
        List<Membership> list = marketingService.listMemberships();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Membership m : list) {
            Map<String, Object> plan = new LinkedHashMap<>();
            plan.put("id", m.getId());
            plan.put("name", m.getMembershipName());
            plan.put("duration", m.getValidDays());
            plan.put("price", m.getPrice());
            plan.put("originalPrice", m.getPrice());
            plan.put("features", m.getDescription() != null && !m.getDescription().isBlank()
                    ? Arrays.asList(m.getDescription().split("[\\n,，]")) : Collections.emptyList());
            plan.put("recommended", m.getStatus() != null && m.getStatus() == 1);
            result.add(plan);
        }
        return Result.success(result);
    }

    @Operation(summary = "购买会员（创建订单）")
    @PostMapping("/membership/purchase")
    public Result<Map<String, Object>> purchaseMembership(@RequestBody PurchaseReq req) {
        if (req.getPlanId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会员套餐ID不能为空");
        }
        var orderVO = orderService.createOrderFrontend(getCurrentUserId(), "membership", req.getPlanId(), null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderVO.getId());
        data.put("orderNo", orderVO.getOrderNo());
        return Result.success(data);
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/coupon/receive")
    public Result<Void> receiveCoupon(@RequestParam Long couponId) {
        marketingService.receiveCoupon(getCurrentUserId(), couponId);
        return Result.success();
    }

    @Operation(summary = "领取优惠券（路径对齐 /coupons/{id}/receive）")
    @PostMapping("/coupons/{couponId}/receive")
    public Result<Void> receiveCouponByPath(@PathVariable Long couponId) {
        marketingService.receiveCoupon(getCurrentUserId(), couponId);
        return Result.success();
    }

    @Operation(summary = "用户优惠券列表")
    @GetMapping("/coupons")
    public Result<List<Coupon>> listUserCoupons() {
        return Result.success(marketingService.listUserCoupons(getCurrentUserId()));
    }

    @Operation(summary = "可用优惠券列表（下单时选择，对齐 /coupons/available）")
    @GetMapping("/coupons/available")
    public Result<List<Map<String, Object>>> availableCoupons(@RequestParam(required = false) String productType,
                                                               @RequestParam(required = false) Long productId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> list = marketingService.listAllCoupons();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Coupon c : list) {
            if (c.getStatus() == null || c.getStatus() != 1) continue;
            if (c.getValidEndTime() != null && c.getValidEndTime().isBefore(now)) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getCouponName());
            m.put("type", c.getCouponType() != null && c.getCouponType() == 2 ? "percent" : "fixed");
            m.put("value", c.getDiscountValue());
            m.put("minAmount", c.getMinAmount());
            m.put("startTime", c.getValidStartTime() == null ? null : c.getValidStartTime().toString());
            m.put("endTime", c.getValidEndTime() == null ? null : c.getValidEndTime().toString());
            m.put("status", "available");
            m.put("received", false);
            result.add(m);
        }
        return Result.success(result);
    }

    /** 购买会员请求体 */
    public static class PurchaseReq {
        private Long planId;
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
    }
}
