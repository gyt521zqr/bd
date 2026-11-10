/**
 * 操作日志记录 Composable
 * 供所有后台管理页面调用，将操作行为持久化到 localStorage
 * OperationLogPage 读取同一份数据展示
 */
import { getLocalData, setLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { useUserStore } from '@/stores/user'

export type LogAction = 'create' | 'update' | 'delete' | 'login' | 'export' | 'import' | 'config' | 'batch_delete' | 'status_change'
export type LogModule = 'student' | 'teacher' | 'course' | 'exam' | 'question' | 'system' | 'registration' | 'score' | 'paper' | 'class' | 'news' | 'banner' | 'file' | 'notification' | 'regcode' | 'practice' | 'marketing' | 'partner' | 'category' | 'homepage' | 'about' | 'storage' | 'backup' | 'trial'
export type LogStatus = 'success' | 'fail'

export interface OperationLogEntry {
  id: number
  operator: string
  module: LogModule
  action: LogAction
  description: string
  ip: string
  status: LogStatus
  operTime: string
  detail?: string
}

/** 生成当前时间字符串 */
function nowStr(): string {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

/**
 * 记录一条操作日志
 * @param module  操作模块
 * @param action  操作类型
 * @param description 操作描述
 * @param status  操作状态（默认 success）
 * @param detail  详细信息（可选）
 */
export function logOperation(
  module: LogModule,
  action: LogAction,
  description: string,
  status: LogStatus = 'success',
  detail?: string,
): void {
  try {
    const userStore = useUserStore()
    const logs = getLocalData<OperationLogEntry[]>(STORAGE_KEYS.OPERATION_LOG_LIST, [])

    const entry: OperationLogEntry = {
      id: logs.length > 0 ? Math.max(...logs.map(l => l.id)) + 1 : 1,
      operator: userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员',
      module,
      action,
      description,
      ip: '127.0.0.1',
      status,
      operTime: nowStr(),
      detail,
    }

    // 最多保留 500 条日志
    logs.unshift(entry)
    if (logs.length > 500) logs.length = 500

    setLocalData(STORAGE_KEYS.OPERATION_LOG_LIST, logs)
  } catch (e) {
    console.warn('[useOperationLog] Failed to log operation:', e)
  }
}

/**
 * Composable 形式，提供便捷的日志记录方法
 */
export function useOperationLog(module: LogModule) {
  const log = (action: LogAction, description: string, status: LogStatus = 'success', detail?: string) => {
    logOperation(module, action, description, status, detail)
  }

  const logCreate = (description: string, detail?: string) => log('create', description, 'success', detail)
  const logUpdate = (description: string, detail?: string) => log('update', description, 'success', detail)
  const logDelete = (description: string, detail?: string) => log('delete', description, 'success', detail)
  const logExport = (description: string, detail?: string) => log('export', description, 'success', detail)
  const logImport = (description: string, detail?: string) => log('import', description, 'success', detail)
  const logConfig = (description: string, detail?: string) => log('config', description, 'success', detail)
  const logBatchDelete = (description: string, detail?: string) => log('batch_delete', description, 'success', detail)
  const logStatusChange = (description: string, detail?: string) => log('status_change', description, 'success', detail)

  return {
    log,
    logCreate,
    logUpdate,
    logDelete,
    logExport,
    logImport,
    logConfig,
    logBatchDelete,
    logStatusChange,
  }
}
