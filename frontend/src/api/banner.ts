/**
 * Banner 公开 API
 * 首页轮播图由后台 cms_banner 表维护，公开端点 GET /api/banner?position=home
 */
import request from './index'

export interface BannerItem {
  id: number
  title: string
  imageUrl?: string
  linkUrl?: string
  position?: string
  sortOrder?: number
  status?: number
}

/** 获取指定位置的 Banner 列表（仅返回启用项，按 sortOrder 升序） */
export function getBanners(position = 'home') {
  return request.get<any, any>('/banner', { params: { position }, silent: true })
}
