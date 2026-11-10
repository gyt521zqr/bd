<template>
  <!-- 数据备份管理 -->
  <div class="data-backup-page">
    <div class="page-header">
      <h2>数据备份</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleBackup">立即备份</el-button>
        <el-button @click="handleExportAll">导出全部数据</el-button>
        <el-upload :show-file-list="false" :before-upload="handleImportFile" accept=".json">
          <el-button>导入数据</el-button>
        </el-upload>
      </div>
    </div>

    <p class="page-desc">管理后台数据的备份与恢复，支持导出/导入全量数据。</p>

    <!-- 存储统计 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.backupCount }}</div>
        <div class="stat-label">备份次数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ formatSize(stats.totalSize) }}</div>
        <div class="stat-label">备份总大小</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.moduleCount }}</div>
        <div class="stat-label">数据模块数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.recordCount }}</div>
        <div class="stat-label">数据记录数</div>
      </div>
    </div>

    <!-- 数据模块概览 -->
    <el-card class="module-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>数据模块概览</span>
          <el-button text type="primary" @click="refreshModules">刷新</el-button>
        </div>
      </template>
      <el-table :data="moduleList" stripe size="small" empty-text="暂无数据">
        <el-table-column prop="name" label="模块名称" min-width="140" />
        <el-table-column prop="key" label="存储键" min-width="180" />
        <el-table-column prop="count" label="记录数" width="100" align="center" />
        <el-table-column prop="size" label="数据大小" width="120" align="center">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column prop="lastUpdate" label="最后更新" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleExportModule(row as ModuleInfo)">导出</el-button>
            <el-button size="small" text type="danger" @click="handleClearModule(row as ModuleInfo)">清空</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 备份记录列表 -->
    <el-card class="backup-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>备份记录</span>
        </div>
      </template>
      <el-table :data="backupList" stripe empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="备份名称" min-width="200" />
        <el-table-column prop="size" label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column prop="moduleCount" label="模块数" width="80" align="center" />
        <el-table-column prop="recordCount" label="记录数" width="80" align="center" />
        <el-table-column prop="createTime" label="备份时间" width="170" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'auto' ? 'info' : 'primary'" size="small">{{ row.type === 'auto' ? '自动' : '手动' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="success" @click="handleRestore(row as BackupItem)">恢复</el-button>
            <el-button size="small" text type="primary" @click="handleDownloadBackup(row as BackupItem)">下载</el-button>
            <el-button size="small" text type="danger" @click="handleDeleteBackup(row as BackupItem)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * DataBackupPage - 数据备份管理
 * 管理后台数据的备份、恢复、导出、导入
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('backup')

interface BackupItem {
  id: number
  name: string
  size: number
  moduleCount: number
  recordCount: number
  createTime: string
  type: string
  data: Record<string, unknown>
}

interface ModuleInfo {
  name: string
  key: string
  count: number
  size: number
  lastUpdate: string
}

const PREFIX = 'magao_'

/** 获取所有数据模块的概览信息 */
function getAllModules(): ModuleInfo[] {
  const modules: { name: string; key: string }[] = [
    { name: '学员数据', key: STORAGE_KEYS.STUDENT_LIST },
    { name: '教师数据', key: STORAGE_KEYS.TEACHER_LIST },
    { name: '课程数据', key: STORAGE_KEYS.COURSE_LIST },
    { name: '赛事数据', key: STORAGE_KEYS.EXAM_LIST },
    { name: '报名数据', key: STORAGE_KEYS.REGISTRATION_LIST },
    { name: '题库数据', key: STORAGE_KEYS.QUESTION_LIST },
    { name: '试卷数据', key: STORAGE_KEYS.PAPER_LIST },
    { name: '文件数据', key: STORAGE_KEYS.FILE_LIST },
    { name: '班级数据', key: STORAGE_KEYS.CLASS_LIST },
    { name: '注册码数据', key: STORAGE_KEYS.REG_CODE_LIST },
    { name: '成绩数据', key: STORAGE_KEYS.SCORE_LIST },
    { name: '试听数据', key: STORAGE_KEYS.TRIAL_LIST },
    { name: '资讯数据', key: STORAGE_KEYS.NEWS_LIST },
    { name: '分类数据', key: STORAGE_KEYS.CATEGORIES },
    { name: '合作伙伴', key: STORAGE_KEYS.PARTNERS },
    { name: 'Banner数据', key: STORAGE_KEYS.BANNER_LIST },
    { name: '通知数据', key: STORAGE_KEYS.NOTIFICATION_LIST },
    { name: '操作日志', key: STORAGE_KEYS.OPERATION_LOG_LIST },
    { name: '首页配置', key: STORAGE_KEYS.HOMEPAGE_CONFIG },
    { name: '关于我们', key: STORAGE_KEYS.ABOUT_CONFIG },
    { name: '系统设置', key: STORAGE_KEYS.SITE_CONFIG },
    { name: '积分设置', key: STORAGE_KEYS.POINTS_CONFIG },
    { name: '通知设置', key: STORAGE_KEYS.NOTIFY_CONFIG },
    { name: '营销数据', key: STORAGE_KEYS.MARKETING },
    { name: '练习题库', key: STORAGE_KEYS.PRACTICE_QUESTIONS },
  ]

  return modules.map(m => {
    const raw = localStorage.getItem(PREFIX + m.key)
    let count = 0
    let size = 0
    if (raw) {
      size = new Blob([raw]).size
      try {
        const parsed = JSON.parse(raw)
        if (Array.isArray(parsed)) count = parsed.length
        else count = Object.keys(parsed).length
      } catch { /* ignore */ }
    }
    return {
      name: m.name,
      key: m.key,
      count,
      size,
      lastUpdate: new Date().toLocaleString(),
    }
  })
}

const moduleList = ref<ModuleInfo[]>([])

function refreshModules() {
  moduleList.value = getAllModules()
}

/** 统计信息 */
const stats = computed(() => {
  const backupCount = backupList.value.length
  const totalSize = backupList.value.reduce((sum, b) => sum + b.size, 0)
  const moduleCount = moduleList.value.filter(m => m.count > 0).length
  const recordCount = moduleList.value.reduce((sum, m) => sum + m.count, 0)
  return { backupCount, totalSize, moduleCount, recordCount }
})

/** 格式化文件大小 */
function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + 'KB'
  if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + 'MB'
  return (bytes / 1073741824).toFixed(2) + 'GB'
}

// 备份记录
const backupList = ref<BackupItem[]>([])

function saveBackupToLocal() {
  setLocalData(STORAGE_KEYS.DATA_BACKUP_LIST, backupList.value)
}

/** 立即备份 */
async function handleBackup() {
  ElMessage.info('正在备份数据...')
  await new Promise(r => setTimeout(r, 800))

  const allData: Record<string, unknown> = {}
  let totalRecords = 0
  let totalSize = 0

  for (const m of moduleList.value) {
    if (m.count > 0) {
      const raw = localStorage.getItem(PREFIX + m.key)
      if (raw) {
        try {
          allData[m.key] = JSON.parse(raw)
          totalRecords += m.count
          totalSize += new Blob([raw]).size
        } catch { /* ignore */ }
      }
    }
  }

  const now = new Date()
  const newId = backupList.value.length > 0 ? Math.max(...backupList.value.map(b => b.id)) + 1 : 1
  const backup: BackupItem = {
    id: newId,
    name: `全量备份_${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`,
    size: totalSize,
    moduleCount: Object.keys(allData).length,
    recordCount: totalRecords,
    createTime: now.toLocaleString(),
    type: 'manual',
    data: allData,
  }
  backupList.value.unshift(backup)
  saveBackupToLocal()
  ElMessage.success(`备份成功，包含 ${backup.moduleCount} 个模块，${backup.recordCount} 条记录`)
  opLog.logCreate('数据全量备份')
}

/** 恢复备份 */
function handleRestore(row: BackupItem) {
  ElMessageBox.confirm(`确定恢复备份 "${row.name}" 吗？这将覆盖当前所有数据！`, '危险操作', {
    type: 'warning',
    confirmButtonText: '确认恢复',
    confirmButtonClass: 'el-button--danger',
  })
    .then(() => {
      let restoredCount = 0
      for (const [key, value] of Object.entries(row.data)) {
        localStorage.setItem(PREFIX + key, JSON.stringify(value))
        restoredCount++
      }
      refreshModules()
      ElMessage.success(`已恢复 ${restoredCount} 个模块的数据，请刷新页面查看`)
      opLog.logUpdate('恢复数据备份')
    })
    .catch(() => {})
}

/** 下载备份文件 */
function handleDownloadBackup(row: BackupItem) {
  const json = JSON.stringify(row.data, null, 2)
  const blob = new Blob([json], { type: 'application/json;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${row.name}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('备份文件已下载')
}

/** 删除备份 */
function handleDeleteBackup(row: BackupItem) {
  ElMessageBox.confirm(`确定删除备份 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(() => {
      backupList.value = backupList.value.filter(b => b.id !== row.id)
      saveBackupToLocal()
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

/** 导出全部数据 */
function handleExportAll() {
  const allData: Record<string, unknown> = {}
  for (const m of moduleList.value) {
    if (m.count > 0) {
      const raw = localStorage.getItem(PREFIX + m.key)
      if (raw) {
        try { allData[m.key] = JSON.parse(raw) } catch { /* ignore */ }
      }
    }
  }
  const json = JSON.stringify(allData, null, 2)
  const blob = new Blob([json], { type: 'application/json;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const now = new Date()
  a.download = `magao_full_data_${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('全部数据已导出')
  opLog.logExport('导出全部数据')
}

/** 导入数据 */
function handleImportFile(file: File): boolean {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = JSON.parse(e.target?.result as string)
      let importCount = 0
      for (const [key, value] of Object.entries(data)) {
        localStorage.setItem(PREFIX + key, JSON.stringify(value))
        importCount++
      }
      refreshModules()
      ElMessage.success(`成功导入 ${importCount} 个模块的数据`)
      opLog.logImport('导入数据')
    } catch {
      ElMessage.error('文件格式错误，请选择有效的JSON备份文件')
    }
  }
  reader.readAsText(file)
  return false
}

/** 导出单个模块 */
function handleExportModule(row: ModuleInfo) {
  const raw = localStorage.getItem(PREFIX + row.key)
  if (!raw) {
    ElMessage.warning('该模块暂无数据')
    return
  }
  const blob = new Blob([raw], { type: 'application/json;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${row.key}_${new Date().toISOString().slice(0, 10)}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success(`模块「${row.name}」已导出`)
}

/** 清空单个模块 */
function handleClearModule(row: ModuleInfo) {
  ElMessageBox.confirm(`确定清空模块「${row.name}」的所有数据吗？此操作不可恢复！`, '危险操作', {
    type: 'error',
    confirmButtonText: '确认清空',
    confirmButtonClass: 'el-button--danger',
  })
    .then(() => {
      localStorage.removeItem(PREFIX + row.key)
      refreshModules()
      ElMessage.success(`模块「${row.name}」已清空`)
    })
    .catch(() => {})
}

onMounted(() => {
  const localData = getLocalData<BackupItem[] | null>(STORAGE_KEYS.DATA_BACKUP_LIST, null)
  if (localData && localData.length > 0) {
    backupList.value = localData
  }
  refreshModules()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.data-backup-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; h2 { font-size: 20px; color: #1D2129; } .header-actions { display: flex; gap: 8px; } }
.page-desc { color: #666; font-size: 13px; margin-bottom: 16px; }
.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}
.module-card { margin-bottom: 20px; }
.backup-card { .card-header { display: flex; justify-content: space-between; align-items: center; } }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; color: #1D2129; }
</style>
