/**
 * 系统公开 API
 * 站点级配置（站名/Logo/ICP/客服/导航/功能开关）与系统公告均由后台下发，
 * 前台在启动时一次性拉取（见 stores/site.ts），不再写死。
 */
import request from './index'

export interface SiteConfigData {
  siteName?: string
  siteLogo?: string
  icp?: string
  contactPhone?: string
  contactEmail?: string
  contactAddress?: string
  copyright?: string
  navMenu?: Array<{ label: string; path: string; icon?: string }>
  registerEnabled?: boolean
  maintenanceMode?: boolean
}

export interface SysNoticeItem {
  id?: number
  title?: string
  content?: string
  noticeType?: number
  status?: number
}

/** 公开站点配置（无需登录） */
export function getSiteConfig() {
  return request.get<any, any>('/system/site-config', { silent: true })
}

/** 系统公告列表（仅返回开启项） */
export function getNotices() {
  return request.get<any, any>('/system/notices', { silent: true })
}

/** 读取单个配置键值（公开） */
export function getConfig(key: string) {
  return request.get<any, any>(`/system/config/${key}`, { silent: true })
}
