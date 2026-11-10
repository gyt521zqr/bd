/**
 * 支付相关类型定义
 */

/** 订单状态 */
export type OrderStatus = 'pending' | 'paid' | 'cancelled' | 'refunded' | 'expired'

/** 支付方式 */
export type PayMethod = 'wechat' | 'alipay' | 'balance'

/** 订单信息 */
export interface Order {
  id: number
  orderNo: string
  userId: number
  productId: number
  productName: string
  productType: 'course' | 'membership' | 'goods'
  payAmount: number
  status: OrderStatus
  payMethod?: PayMethod
  payTime?: string
  createTime: string
}

/** 优惠券类型 */
export type CouponType = 'fixed' | 'percent'

/** 优惠券信息 */
export interface Coupon {
  id: number
  name: string
  description: string
  type: CouponType
  value: number
  minAmount: number
  expireTime: string
  status: 'available' | 'used' | 'expired'
}

/** 会员套餐 */
export interface MembershipPlan {
  id: number
  name: string
  duration: number
  price: number
  features: string[]
}

/** 订单列表项 */
export interface OrderItem {
  id: number
  orderNo: string
  userId: number
  productId: number
  productName: string
  productType: 'course' | 'membership' | 'goods'
  payAmount: number
  status: OrderStatus
  payMethod?: PayMethod
  payTime?: string
  createTime: string
}

/** 支付表单 */
export interface PayForm {
  orderId: number
  payMethod: PayMethod
}
