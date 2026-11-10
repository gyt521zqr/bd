/**
 * 阿里云存储工具模块
 * 统一封装OSS和VOD的URL生成、CDN加速、缓存策略
 */

import { getLocalData, STORAGE_KEYS } from './localStore'

// ==================== 存储配置 ====================

/** 阿里云OSS配置 */
export interface OSSConfig {
  /** Bucket域名，如 https://magao-edu.oss-cn-beijing.aliyuncs.com */
  domain: string
  /** CDN加速域名（可选），如 https://cdn.magaoedu.com */
  cdnDomain?: string
  /** 是否启用HTTPS */
  secure: boolean
}

/** 阿里云VOD配置 */
export interface VODConfig {
  /** 播放域名 */
  playDomain: string
  /** CDN加速域名 */
  cdnDomain?: string
}

/** 存储类型 */
export type StorageType = 'oss' | 'vod' | 'local'

// 默认配置（正式上线前需通过后台管理页面配置真实域名）
const DEFAULT_OSS_CONFIG: OSSConfig = {
  domain: 'https://magao-edu.oss-cn-beijing.aliyuncs.com',
  cdnDomain: 'https://cdn.magaoedu.com',
  secure: true,
}

const DEFAULT_VOD_CONFIG: VODConfig = {
  playDomain: 'https://vod.magaoedu.com',
  cdnDomain: 'https://vod-cdn.magaoedu.com',
}

// 开发环境警告：提醒配置真实存储域名
if (import.meta.env.DEV) {
  const storedOSS = getLocalData<OSSConfig | null>(STORAGE_KEYS.OSS_CONFIG, null)
  if (!storedOSS) {
    console.warn('[storage] OSS/VOD 使用默认占位域名，请在后台管理 > 存储配置中设置真实域名')
  }
}

// ==================== 配置管理 ====================

/** 获取OSS配置（支持从localStorage读取后台配置） */
export function getOSSConfig(): OSSConfig {
  const stored = getLocalData<OSSConfig | null>(STORAGE_KEYS.OSS_CONFIG, null)
  return stored || DEFAULT_OSS_CONFIG
}

/** 获取VOD配置 */
export function getVODConfig(): VODConfig {
  const stored = getLocalData<VODConfig | null>(STORAGE_KEYS.VOD_CONFIG, null)
  return stored || DEFAULT_VOD_CONFIG
}

// ==================== URL生成 ====================

/**
 * 生成OSS访问URL
 * @param path 文件路径（不含域名），如 'course/covers/python-intro.jpg'
 * @param options 选项
 */
export function getOSSUrl(
  path: string,
  options?: {
    /** 指定宽度，生成缩略图 */
    width?: number
    /** 指定高度 */
    height?: number
    /** 图片质量 1-100 */
    quality?: number
    /** 是否使用CDN */
    useCDN?: boolean
    /** 自定义过期时间（秒），用于私有Bucket */
    expires?: number
  }
): string {
  if (!path) return ''

  // 如果已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  const config = getOSSConfig()
  const domain = options?.useCDN !== false && config.cdnDomain
    ? config.cdnDomain
    : config.domain

  // 去除路径前导斜杠
  const cleanPath = path.startsWith('/') ? path.slice(1) : path

  let url = `${domain}/${cleanPath}`

  // 图片处理参数（阿里云OSS图片处理）
  if (options?.width || options?.height || options?.quality) {
    const params: string[] = []
    if (options.width) params.push(`w_${options.width}`)
    if (options.height) params.push(`h_${options.height}`)
    if (options.quality) params.push(`q_${options.quality}`)
    if (params.length > 0) {
      url += `?x-oss-process=image/resize,${params.join(',')}`
    }
  }

  return url
}

/**
 * 生成VOD播放URL
 * @param videoId 视频ID（阿里云VOD的VideoId）
 * @param options 选项
 */
export function getVODUrl(
  videoId: string,
  options?: {
    /** 播放格式：mp4、m3u8 */
    format?: 'mp4' | 'm3u8'
    /** 清晰度：ld(流畅)、sd(标清)、hd(高清)、fhd(超清) */
    definition?: 'ld' | 'sd' | 'hd' | 'fhd'
    /** 是否使用CDN */
    useCDN?: boolean
  }
): string {
  if (!videoId) return ''

  // 如果已经是完整URL，直接返回
  if (videoId.startsWith('http://') || videoId.startsWith('https://')) {
    return videoId
  }

  const config = getVODConfig()
  const domain = options?.useCDN !== false && config.cdnDomain
    ? config.cdnDomain
    : config.playDomain

  const format = options?.format || 'm3u8'
  const definition = options?.definition || 'hd'

  // 阿里云VOD播放地址格式
  return `${domain}/${videoId}/${definition}.${format}`
}

/**
 * 获取视频封面图URL（VOD截图）
 * @param videoId 视频ID
 * @param options 截图选项
 */
export function getVODCoverUrl(
  videoId: string,
  options?: {
    /** 截图时间点（秒） */
    time?: number
    /** 宽度 */
    width?: number
    /** 高度 */
    height?: number
  }
): string {
  if (!videoId) return ''

  const config = getVODConfig()
  const domain = config.cdnDomain || config.playDomain

  const time = options?.time || 0
  const width = options?.width || 640
  const height = options?.height || 360

  return `${domain}/${videoId}/cover_${time}_${width}x${height}.jpg`
}

// ==================== 路径生成 ====================

/** 文件类型对应的OSS目录 */
const OSS_DIRS: Record<string, string> = {
  'course-cover': 'course/covers',
  'course-video': 'course/videos',
  'news-cover': 'news/covers',
  'exam-cover': 'exam/covers',
  'avatar': 'user/avatars',
  'team-avatar': 'about/team',
  'partner-logo': 'partner/logos',
  'banner': 'home/banners',
  'scratch': 'scratch/projects',
  'scratch-asset': 'scratch/assets',
  'temp': 'temp',
}

/**
 * 生成OSS上传路径
 * @param type 文件类型
 * @param fileName 原始文件名
 * @param id 关联ID（如用户ID、课程ID）
 */
export function generateOSSPath(
  type: keyof typeof OSS_DIRS,
  fileName: string,
  id?: number | string
): string {
  const dir = OSS_DIRS[type] || 'misc'
  const timestamp = Date.now()
  const random = Math.random().toString(36).substring(2, 8)
  const safeName = fileName.replace(/[^a-zA-Z0-9\u4e00-\u9fa5._-]/g, '_')

  if (id !== undefined) {
    return `${dir}/${id}/${timestamp}_${random}_${safeName}`
  }
  return `${dir}/${timestamp}_${random}_${safeName}`
}

// ==================== 缓存策略 ====================

/**
 * 获取资源URL（带缓存控制）
 * 用于需要强制刷新缓存的场景
 */
export function getCachedUrl(url: string, bustCache?: boolean): string {
  if (!url) return ''
  if (!bustCache) return url

  const sep = url.includes('?') ? '&' : '?'
  return `${url}${sep}_t=${Date.now()}`
}

/**
 * 预加载图片资源
 * @param urls 图片URL列表
 */
export function preloadImages(urls: string[]): Promise<void[]> {
  return Promise.all(
    urls
      .filter(Boolean)
      .map(
        (url) =>
          new Promise<void>((resolve) => {
            const img = new Image()
            img.onload = () => resolve()
            img.onerror = () => resolve()
            img.src = url
          })
      )
  )
}

/**
 * 批量获取OSS URL
 * @param paths 路径列表
 * @param options 选项
 */
export function batchGetOSSUrls(
  paths: string[],
  options?: Parameters<typeof getOSSUrl>[1]
): string[] {
  return paths.map((path) => getOSSUrl(path, options))
}

// ==================== 文件类型判断 ====================

/** 判断URL是否为OSS资源 */
export function isOSSUrl(url: string): boolean {
  if (!url) return false
  const config = getOSSConfig()
  return url.includes(config.domain) || (config.cdnDomain ? url.includes(config.cdnDomain) : false)
}

/** 判断URL是否为VOD资源 */
export function isVODUrl(url: string): boolean {
  if (!url) return false
  const config = getVODConfig()
  return url.includes(config.playDomain) || (config.cdnDomain ? url.includes(config.cdnDomain) : false)
}

/** 判断是否为本地/相对路径 */
export function isLocalPath(path: string): boolean {
  if (!path) return true
  return !path.startsWith('http://') && !path.startsWith('https://')
}

// ==================== 导出常量 ====================

/** 图片压缩预设 */
export const IMAGE_PRESETS = {
  /** 缩略图：小尺寸 */
  thumbnail: { width: 200, height: 150, quality: 80 },
  /** 列表图：中等尺寸 */
  list: { width: 400, height: 300, quality: 85 },
  /** 封面图：大尺寸 */
  cover: { width: 800, height: 600, quality: 90 },
  /** 头像：正方形 */
  avatar: { width: 200, height: 200, quality: 85 },
  /** Banner：宽图 */
  banner: { width: 1920, height: 600, quality: 85 },
} as const

/** 视频清晰度预设 */
export const VIDEO_DEFINITIONS = {
  ld: { label: '流畅', definition: 'ld' as const, width: 640, height: 360 },
  sd: { label: '标清', definition: 'sd' as const, width: 960, height: 540 },
  hd: { label: '高清', definition: 'hd' as const, width: 1280, height: 720 },
  fhd: { label: '超清', definition: 'fhd' as const, width: 1920, height: 1080 },
} as const
