/**
 * 站点全局配置 store
 * --------------------------------------------------
 * 在应用启动时（main.ts）通过 loadSiteConfig() 一次性拉取：
 *   - 站点基础信息（站名 / Logo / ICP / 客服 / 版权）
 *   - 顶部导航菜单（后台可配，nav_menu JSON）
 *   - 功能开关（注册开关、维护模式）
 *   - 系统公告（用于全局公告条）
 * 各页面/布局统一从此处读取，替代写死的文案与分散调用。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSiteConfig, getNotices, getConfig, type SysNoticeItem } from '@/api/system'

export interface NavItem {
  label: string
  path: string
  icon?: string
}

/** 默认导航（后台未配置 nav_menu 时回退） */
export const DEFAULT_NAV: NavItem[] = [
  { label: '首页', path: '/' },
  { label: '赛事中心', path: '/exam' },
  { label: '题库中心', path: '/question-bank' },
  { label: '课程中心', path: '/course' },
  { label: '创作社区', path: '/community' },
  { label: '会员中心', path: '/membership' },
  { label: '关于我们', path: '/about' },
]

export interface SiteConfig {
  siteName: string
  siteLogo: string
  icp: string
  contactPhone: string
  contactEmail: string
  contactAddress: string
  copyright: string
  navMenu: NavItem[]
  registerEnabled: boolean
  maintenanceMode: boolean
}

const defaultConfig: SiteConfig = {
  siteName: '码高教育',
  siteLogo: '',
  icp: '',
  contactPhone: '',
  contactEmail: '',
  contactAddress: '',
  copyright: 'Copyright 2024-2026 码高少儿编程教育平台',
  navMenu: DEFAULT_NAV,
  registerEnabled: true,
  maintenanceMode: false,
}

export const useSiteStore = defineStore('site', () => {
  const config = ref<SiteConfig>({ ...defaultConfig })
  const notices = ref<SysNoticeItem[]>([])
  const loaded = ref(false)

  /** 合并后端配置（忽略 null/undefined，避免覆盖本地默认值；navMenu 空数组表示用默认导航） */
  function applyConfig(data: Partial<SiteConfig>) {
    const cleaned = Object.fromEntries(
      Object.entries(data).filter(([, v]) => v !== null && v !== undefined)
    ) as Partial<SiteConfig>
    const next: SiteConfig = { ...config.value, ...cleaned }
    if (Array.isArray(cleaned.navMenu) && cleaned.navMenu.length > 0) {
      next.navMenu = cleaned.navMenu
    }
    config.value = next
  }

  /** 启动时拉取站点配置 + 公告 */
  async function loadSiteConfig() {
    try {
      const res = await getSiteConfig()
      if (res?.code === 200 && res.data) {
        applyConfig(res.data as Partial<SiteConfig>)
      }
    } catch {
      // 网络异常时沿用默认配置，不阻塞前台
    }
    try {
      const n = await getNotices()
      if (n?.code === 200 && Array.isArray(n.data)) {
        notices.value = n.data
      }
    } catch {
      // 公告缺失不阻塞
    }
    loaded.value = true
  }

  /**
   * 通用功能开关：按需读取后台 sys_config 的任意 key（GET /api/system/config/{key}）
   * 内置快捷开关（registerEnabled / maintenanceMode）已在 config 中聚合下发，
   * 其余模块级开关可用此方法按 key 获取，无需新增接口。
   */
  async function getFlag(key: string, defaultValue = false): Promise<boolean> {
    try {
      const res = await getConfig(key)
      if (res?.code === 200) {
        const v = res.data
        return v === true || v === 'true' || v === '1' || v === 1
      }
    } catch {
      // 配置缺失或接口不可用：沿用默认值
    }
    return defaultValue
  }

  return { config, notices, loaded, loadSiteConfig, applyConfig, getFlag }
})
