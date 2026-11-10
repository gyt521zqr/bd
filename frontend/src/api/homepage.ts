/**
 * 首页配置管理 API 接口
 */
import request from './index'

export interface HomepageStats {
  userCount: string
  activeStudents: string
  coveredCities: string
  partnerCount: string
  workCount: string
}

export interface HomepageEntry {
  id: number
  title: string
  description: string
  icon: string
  link: string
  bgColor: string
  coverUrl?: string
}

export interface HomepageMedia {
  id: number
  name: string
  icon: string
  link: string
  bgColor: string
}

export interface HomepageConfig {
  stats: HomepageStats
  entries: HomepageEntry[]
  media: HomepageMedia[]
  /** 轮播是否自动播放 */
  bannerAutoPlay?: boolean
  /** 轮播间隔（毫秒） */
  bannerInterval?: number
  /** 是否显示课程分类/学习中心区块 */
  showCourseCategory?: boolean
  /** 是否显示资讯区块 */
  showNews?: boolean
  /** 是否显示合作伙伴区块 */
  showPartner?: boolean
}

/**
 * 获取首页配置。
 * 后端 sys 配置为唯一数据源；接口不可用时返回 null，由调用方显示空态，
 * 不再回退到浏览器 localStorage（那会让前台展示管理员本地的旧数据或虚构示例数据）。
 */
export async function getHomepageConfig() {
  try {
    return await request.get<any, { code: number; data: HomepageConfig; message?: string }>('/homepage/config')
  } catch {
    return null
  }
}

/** 更新首页配置 */
export function updateHomepageConfig(data: HomepageConfig) {
  return request.put<any, { code: number; data: HomepageConfig; message?: string }>('/homepage/config', data)
}
