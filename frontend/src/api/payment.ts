/**
 * 支付相关 API 接口
 */
import request from './index'
import type { Result, PageResult, PageQuery, OrderItem, PayForm } from '@/types'

/** 创建订单 */
export function createOrder(data: { productType: string; productId: number; couponId?: number }) {
  return request.post<any, Result<OrderItem>>('/payment/order/create', data)
}

/** 查询订单列表 */
export function getOrderList(params: PageQuery & { status?: string }) {
  return request.get<any, Result<PageResult<OrderItem>>>('/payment/order/list', { params })
}

/** 查询订单详情 */
export function getOrderDetail(orderId: number) {
  return request.get<any, Result<OrderItem>>(`/payment/order/detail/${orderId}`)
}

/** 发起支付 */
export function payOrder(data: PayForm) {
  return request.post<any, Result<{ payUrl: string; qrCode: string }>>('/payment/pay', data)
}

/** 轮询支付状态 */
export function pollPayStatus(orderId: number) {
  return request.get<any, Result<{ status: string; payTime?: string }>>(`/payment/status/${orderId}`)
}

/** 取消订单 */
export function cancelOrder(orderId: number) {
  return request.put<any, Result<null>>(`/payment/order/${orderId}/cancel`)
}

/** 申请退款 */
export function refundOrder(orderId: number, reason: string) {
  return request.post<any, Result<null>>(`/payment/order/${orderId}/refund`, { reason })
}
