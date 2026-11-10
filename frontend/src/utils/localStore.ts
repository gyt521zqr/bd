/**
 * 本地存储工具
 * 用于后台管理数据的持久化存储，前台页面优先读取
 * 当后端 API 可用时，自动切换为 API 模式
 */

const PREFIX = 'magao_'

// 设置值
export function setLocalData<T>(key: string, data: T): void {
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify(data))
  } catch (e) {
    console.warn(`[localStore] Failed to save ${key}:`, e)
  }
}

// 获取值（重载：支持不传 fallback）
export function getLocalData<T>(key: string, fallback: T): T
export function getLocalData<T>(key: string): T | null
export function getLocalData<T>(key: string, fallback?: T): T | null {
  try {
    const raw = localStorage.getItem(PREFIX + key)
    if (raw) {
      return JSON.parse(raw) as T
    }
  } catch (e) {
    console.warn(`[localStore] Failed to load ${key}:`, e)
  }
  return fallback ?? null
}

// 删除值
export function removeLocalData(key: string): void {
  localStorage.removeItem(PREFIX + key)
}

// 检查是否有本地数据
export function hasLocalData(key: string): boolean {
  return localStorage.getItem(PREFIX + key) !== null
}

// 兼容旧版命名
export const setLocal = setLocalData
export const getLocal = getLocalData
export const removeLocal = removeLocalData

// 存储键名常量
export const STORAGE_KEYS = {
  NEWS_LIST: 'news_list',
  CATEGORIES: 'categories',
  PARTNERS: 'partners',
  HOMEPAGE_CONFIG: 'homepage_config',
  ABOUT_CONFIG: 'about_config',
  BANNERS: 'banners',
  BANNER_LIST: 'banner_list',
  COURSES: 'courses',
  EXAMS: 'exams',
  PRACTICE_QUESTIONS: 'practice_questions',
  SCORE_LIST: 'score_list',
  TRIAL_LIST: 'trial_list',
  MARKETING: 'marketing',
  SYSTEM_SETTINGS: 'system_settings',
  /** 基本设置（站点配置） */
  SITE_CONFIG: 'site_config',
  /** 积分设置 */
  POINTS_CONFIG: 'points_config',
  /** 通知设置 */
  NOTIFY_CONFIG: 'notify_config',
  /** 阿里云OSS配置 */
  OSS_CONFIG: 'oss_config',
  /** 阿里云VOD配置 */
  VOD_CONFIG: 'vod_config',
  /** 学员列表 */
  STUDENT_LIST: 'student_list',
  /** 教师列表 */
  TEACHER_LIST: 'teacher_list',
  /** 课程列表 */
  COURSE_LIST: 'course_list',
  /** 赛事列表 */
  EXAM_LIST: 'exam_list',
  /** 报名列表 */
  REGISTRATION_LIST: 'registration_list',
  /** 题库列表 */
  QUESTION_LIST: 'question_list',
  /** 试卷列表 */
  PAPER_LIST: 'paper_list',
  /** 文件列表 */
  FILE_LIST: 'file_list',
  /** 班级列表 */
  CLASS_LIST: 'class_list',
  /** 注册码列表 */
  REG_CODE_LIST: 'reg_code_list',
  /** 通知消息列表 */
  NOTIFICATION_LIST: 'notification_list',
  /** 操作日志列表 */
  OPERATION_LOG_LIST: 'operation_log',
  /** 数据备份记录 */
  DATA_BACKUP_LIST: 'data_backup_list',
  /** 轮播图/Banner配置 */
  HOMEPAGE_BANNERS: 'homepage_banners',
  /** 友情链接列表 */
  FRIEND_LINKS: 'friend_links',
  /** 管理员账号列表 */
  ADMIN_ACCOUNT_LIST: 'admin_account_list',
} as const
