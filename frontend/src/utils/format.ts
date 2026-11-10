/**
 * 格式化工具函数
 * 提供日期、金额、文件大小等格式化方法
 */
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 初始化dayjs中文配置
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/**
 * 格式化日期
 * @param date 日期字符串或时间戳
 * @param format 格式模板，默认'YYYY-MM-DD HH:mm:ss'
 */
export function formatDate(date: string | number | Date, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化相对时间（如"3分钟前"）
 */
export function formatRelativeTime(date: string | number | Date): string {
  if (!date) return ''
  return dayjs(date).fromNow()
}

/**
 * 格式化金额（分转元）
 * @param cents 金额（分）
 * @param showSymbol 是否显示货币符号
 */
export function formatMoney(cents: number, showSymbol: boolean = true): string {
  const yuan = (cents / 100).toFixed(2)
  return showSymbol ? `¥${yuan}` : yuan
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 */
export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + 'KB'
  if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + 'MB'
  return (bytes / 1073741824).toFixed(1) + 'GB'
}

/**
 * 格式化时长（秒转为可读格式）
 */
export function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) return `${h}小时${m}分${s}秒`
  if (m > 0) return `${m}分${s}秒`
  return `${s}秒`
}
