/**
 * 娑堟伅鐩稿叧 API 鎺ュ彛
 */
import request from './index'
import type { Result, PageResult, PageQuery } from '@/types'

/** 娑堟伅椤圭被鍨?*/
export interface MessageItem {
  id: number
  title: string
  content: string
  type: 'system' | 'course' | 'exam' | 'payment'
  read: boolean
  createTime: string
}

/** 鑾峰彇娑堟伅鍒楄〃 */
export function getMessageList(params: PageQuery & { type?: string; read?: boolean }) {
  return request.get<any, Result<PageResult<MessageItem>>>('/message/list', { params })
}

/** 鏍囪娑堟伅宸茶 */
export function markMessageRead(messageId: number) {
  return request.put<any, Result<null>>(`/message/${messageId}/read`)
}

/** 鍏ㄩ儴鏍囪宸茶 */
export function markAllMessageRead() {
  return request.put<any, Result<null>>('/message/read-all')
}

/** 鑾峰彇鏈娑堟伅鏁伴噺 */
export function getUnreadCount() {
  return request.get<any, Result<{ count: number; details: Record<string, number> }>>('/message/unread-count', { silent: true })
}

/** 鍒犻櫎娑堟伅 */
export function deleteMessage(messageId: number) {
  return request.delete<any, Result<null>>(`/message/${messageId}`)
}
