/**
 * 钀ラ攢鐩稿叧 API 鎺ュ彛锛堜細鍛樸€佷紭鎯犲埜銆佸洟璐級
 */
import request from './index'
import type { Result } from '@/types'

/** 浼氬憳濂楅 */
export interface MembershipPlan {
  id: number
  name: string
  duration: number
  price: number
  originalPrice: number
  features: string[]
  recommended: boolean
}

/** 浼樻儬鍒?*/
export interface CouponItem {
  id: number
  name: string
  type: 'fixed' | 'percent'
  value: number
  minAmount: number
  startTime: string
  endTime: string
  status: 'available' | 'used' | 'expired'
  received: boolean
}

/** 鑾峰彇浼氬憳濂楅鍒楄〃 */
export function getMembershipPlans() {
  return request.get<any, Result<MembershipPlan[]>>('/marketing/membership/plans')
}

/** 璐拱浼氬憳 */
export function purchaseMembership(planId: number) {
  return request.post<any, Result<{ orderId: number }>>('/marketing/membership/purchase', { planId })
}

/** 鑾峰彇浼樻儬鍒稿垪琛?*/
export function getCouponList(params: { status?: string }) {
  return request.get<any, Result<CouponItem[]>>('/marketing/coupons', { params })
}

/** 棰嗗彇浼樻儬鍒?*/
export function receiveCoupon(couponId: number) {
  return request.post<any, Result<null>>(`/marketing/coupons/${couponId}/receive`)
}

/** 鑾峰彇鍙敤浼樻儬鍒革紙涓嬪崟鏃堕€夋嫨锛?*/
export function getAvailableCoupons(productType: string, productId: number) {
  return request.get<any, Result<CouponItem[]>>('/marketing/coupons/available', { params: { productType, productId } })
}
