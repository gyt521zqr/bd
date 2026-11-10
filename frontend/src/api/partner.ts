/**
 * 合作伙伴管理 API 接口
 */
import request from './index'

export interface PartnerItem {
  id: number
  name: string
  type: 'school' | 'museum' | 'research'
  logo?: string
  link?: string
  sortOrder: number
  status: 'enabled' | 'disabled'
}

/**
 * 获取合作伙伴列表。
 * 后端 cms_partner 为唯一数据源；接口不可用时返回 null，由调用方显示空态，
 * 不再回退到浏览器 localStorage（避免展示虚构的合作机构）。
 */
export async function getPartners(params?: { type?: string }) {
  try {
    return await request.get<any, { code: number; data: PartnerItem[]; message?: string }>('/partners', { params })
  } catch {
    return null
  }
}

/** 创建合作伙伴 */
export function createPartner(data: Partial<PartnerItem>) {
  return request.post<any, { code: number; data: PartnerItem; message?: string }>('/partners', data)
}

/** 更新合作伙伴 */
export function updatePartner(id: number, data: Partial<PartnerItem>) {
  return request.put<any, { code: number; data: PartnerItem; message?: string }>(`/partners/${id}`, data)
}

/** 删除合作伙伴 */
export function deletePartner(id: number) {
  return request.delete<any, { code: number; message?: string }>(`/partners/${id}`)
}

/** 更新合作伙伴排序 */
export function updatePartnerSort(id: number, sortOrder: number) {
  return request.put<any, { code: number; message?: string }>(`/partners/${id}/sort`, { sortOrder })
}
