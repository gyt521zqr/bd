/**
 * 关于我们配置管理 API 接口
 */
import request from './index'

export interface TimelineItem {
  id: number
  year: string
  title: string
  description: string
  sortOrder: number
}

export interface TeamMember {
  id: number
  name: string
  position: string
  description: string
  avatar: string
}

export interface AboutConfig {
  title: string
  content: string
  foundedYear: string
  timeline: TimelineItem[]
  team: TeamMember[]
}

/**
 * 获取关于我们配置。
 * 后端为唯一数据源；接口不可用时返回 null，由前台显示空态，
 * 不再回退到浏览器 localStorage（避免展示管理员本地缓存或虚构示例内容）。
 */
export async function getAboutConfig() {
  try {
    return await request.get<any, { code: number; data: AboutConfig; message?: string }>('/about/config')
  } catch {
    return null
  }
}

/** 更新关于我们配置 */
export function updateAboutConfig(data: AboutConfig) {
  return request.put<any, { code: number; data: AboutConfig; message?: string }>('/about/config', data)
}
