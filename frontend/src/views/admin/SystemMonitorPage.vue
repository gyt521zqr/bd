<template>
  <!-- 系统监控 -->
  <div class="system-monitor-page">
    <div class="page-header">
      <h2>系统监控</h2>
      <el-button type="primary" :icon="Refresh" @click="refreshAll">刷新全部</el-button>
    </div>
    <p class="page-desc">实时监控平台运行状态、数据存储和系统健康度</p>

    <el-tabs v-model="activeTab" class="monitor-tabs">
      <!-- Tab 1: 运行状态 -->
      <el-tab-pane label="运行状态" name="runtime">
        <div class="stat-cards">
          <div class="stat-card online-card">
            <div class="stat-value" :class="isOnline ? 'online' : 'offline'">
              <span class="dot" :class="isOnline ? 'dot-online' : 'dot-offline'"></span>
              {{ isOnline ? '在线' : '离线' }}
            </div>
            <div class="stat-label">网络状态</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ systemInfo.browser }}</div>
            <div class="stat-label">浏览器</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ systemInfo.screen }}</div>
            <div class="stat-label">屏幕分辨率</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ systemInfo.deviceType }}</div>
            <div class="stat-label">设备类型</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ systemInfo.os }}</div>
            <div class="stat-label">操作系统</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ systemInfo.language }}</div>
            <div class="stat-label">语言</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ systemInfo.cpuCores }}</div>
            <div class="stat-label">CPU核心数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ systemInfo.memory }}</div>
            <div class="stat-label">内存</div>
          </div>
        </div>
        <div class="action-bar">
          <el-button :icon="Refresh" @click="refreshSystemInfo">刷新系统信息</el-button>
          <span class="update-time">检测时间：{{ lastCheckTime }}</span>
        </div>
      </el-tab-pane>

      <!-- Tab 2: 数据存储 -->
      <el-tab-pane label="数据存储" name="storage">
        <div class="stat-cards">
          <div class="stat-card">
            <div class="stat-value">{{ formatSize(storageStats.totalSize) }}</div>
            <div class="stat-label">已用存储</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ storageStats.totalRecords }}</div>
            <div class="stat-label">数据记录数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ storageStats.usedModules }}</div>
            <div class="stat-label">已用模块数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ formatSize(STORAGE_LIMIT - storageStats.totalSize) }}</div>
            <div class="stat-label">剩余空间</div>
          </div>
        </div>

        <el-card class="module-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>存储使用率</span>
              <span class="usage-text">{{ formatSize(storageStats.totalSize) }} / 5MB ({{ storageStats.percent }}%)</span>
            </div>
          </template>
          <el-progress :percentage="storageStats.percent" :color="progressColor" :stroke-width="16" />
        </el-card>

        <el-card class="module-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>数据模块明细</span>
              <el-button type="danger" text :icon="Delete" @click="handleClearCache">清理缓存</el-button>
            </div>
          </template>
          <el-table :data="moduleList" stripe size="small">
            <el-table-column prop="name" label="模块名称" min-width="140" />
            <el-table-column prop="key" label="存储键" min-width="180" />
            <el-table-column prop="count" label="记录数" width="100" align="center" />
            <el-table-column label="数据大小" width="120" align="center">
              <template #default="{ row }">{{ formatSize(row.size) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.count > 0 ? 'success' : 'info'" size="small">
                  {{ row.count > 0 ? '有数据' : '空' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- Tab 3: 功能清单 -->
      <el-tab-pane label="功能清单" name="features">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>后台模块清单</span>
              <el-button text type="primary" :icon="Refresh" @click="refreshFeatures">刷新</el-button>
            </div>
          </template>
          <el-table :data="featureList" stripe size="small">
            <el-table-column prop="name" label="模块名称" min-width="150" />
            <el-table-column prop="route" label="路由路径" min-width="180" />
            <el-table-column prop="count" label="数据条数" width="100" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'normal' ? 'success' : 'info'" size="small" effect="light">
                  {{ row.status === 'normal' ? '正常' : '未使用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastOperTime" label="最后操作时间" width="180" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- Tab 4: 系统日志摘要 -->
      <el-tab-pane label="系统日志摘要" name="logs">
        <div class="stat-cards">
          <div class="stat-card">
            <div class="stat-value">{{ logStats.total }}</div>
            <div class="stat-label">日志总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value" style="color: #165DFF;">{{ logStats.today }}</div>
            <div class="stat-label">今日日志</div>
          </div>
          <div class="stat-card">
            <div class="stat-value" style="color: #36D399;">{{ logStats.successRate }}%</div>
            <div class="stat-label">成功率</div>
          </div>
          <div class="stat-card">
            <div class="stat-value" style="font-size: 20px;">{{ logStats.mostActive }}</div>
            <div class="stat-label">最活跃模块</div>
          </div>
        </div>

        <div class="log-layout">
          <el-card class="dist-card" shadow="never">
            <template #header><div class="card-header"><span>模块日志分布</span></div></template>
            <div class="dist-list">
              <div v-if="moduleDist.length === 0" class="empty-tip">暂无日志数据</div>
              <div v-for="item in moduleDist" :key="item.module" class="dist-item">
                <span class="dist-label">{{ item.label }}</span>
                <div class="dist-bar-wrap">
                  <div class="dist-bar" :style="{ width: item.percent + '%' }"></div>
                </div>
                <span class="dist-count">{{ item.count }}</span>
              </div>
            </div>
          </el-card>

          <el-card class="recent-card" shadow="never">
            <template #header><div class="card-header"><span>最近 10 条操作</span></div></template>
            <el-table :data="recentLogs" stripe size="small" max-height="360">
              <el-table-column prop="operator" label="操作人" width="90" />
              <el-table-column prop="description" label="操作内容" min-width="180" show-overflow-tooltip />
              <el-table-column label="状态" width="70" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
                    {{ row.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="operTime" label="时间" width="160" />
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
/**
 * SystemMonitorPage - 系统监控
 * 监控平台运行状态、数据存储、功能清单和系统日志摘要
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete } from '@element-plus/icons-vue'
import { getLocalData, removeLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { useOperationLog } from '@/composables/useOperationLog'
import type { OperationLogEntry } from '@/composables/useOperationLog'

const opLog = useOperationLog('system')

const PREFIX = 'magao_'
const STORAGE_LIMIT = 5 * 1024 * 1024 // 5MB

const activeTab = ref('runtime')
const lastCheckTime = ref('')

// 模块标签映射（用于日志分布展示）
const moduleLabelMap: Record<string, string> = {
  student: '学员管理', teacher: '教师管理', course: '课程管理', exam: '赛事管理',
  question: '题库管理', system: '系统设置', registration: '报名管理', score: '成绩管理',
  paper: '试卷管理', class: '班级管理', news: '资讯管理', banner: 'Banner管理',
  file: '文件管理', notification: '通知管理', regcode: '注册码', practice: '练习管理',
  marketing: '营销管理', partner: '合作伙伴', category: '分类管理', homepage: '首页配置',
  about: '关于我们', storage: '存储配置', backup: '备份管理', trial: '试听管理',
}

// ===== Tab 1: 运行状态 =====
interface SystemInfo {
  browser: string
  screen: string
  deviceType: string
  os: string
  language: string
  cpuCores: number | string
  memory: string
}

const isOnline = ref(navigator.onLine)
const systemInfo = ref<SystemInfo>({
  browser: '-', screen: '-', deviceType: '-', os: '-', language: '-', cpuCores: '-', memory: '-',
})

function detectBrowser(): string {
  const ua = navigator.userAgent
  if (ua.includes('Edg/')) return 'Microsoft Edge'
  if (ua.includes('Chrome/')) return 'Google Chrome'
  if (ua.includes('Firefox/')) return 'Mozilla Firefox'
  if (ua.includes('Safari/') && !ua.includes('Chrome')) return 'Safari'
  return ua
}

function detectOS(): string {
  const ua = navigator.userAgent
  if (ua.includes('Windows')) return 'Windows'
  if (ua.includes('Mac OS')) return 'macOS'
  if (ua.includes('Android')) return 'Android'
  if (ua.includes('iPhone') || ua.includes('iPad')) return 'iOS'
  if (ua.includes('Linux')) return 'Linux'
  return navigator.platform || '未知'
}

function detectDeviceType(): string {
  const ua = navigator.userAgent
  if (/Mobi|Android|iPhone|iPad/i.test(ua)) return '移动端'
  return '桌面端'
}

function refreshSystemInfo() {
  const nav = navigator as Navigator & { deviceMemory?: number }
  systemInfo.value = {
    browser: detectBrowser(),
    screen: `${window.screen.width} × ${window.screen.height}`,
    deviceType: detectDeviceType(),
    os: detectOS(),
    language: navigator.language || '未知',
    cpuCores: navigator.hardwareConcurrency || '不可用',
    memory: nav.deviceMemory ? `${nav.deviceMemory} GB` : '不可用',
  }
  lastCheckTime.value = new Date().toLocaleString()
}

function handleOnline() { isOnline.value = true }
function handleOffline() { isOnline.value = false }

// ===== Tab 2: 数据存储 =====
interface ModuleInfo {
  name: string
  key: string
  count: number
  size: number
}

/** 数据模块定义（与 DataBackupPage 保持一致） */
const moduleDefinitions: { name: string; key: string }[] = [
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
  { name: '数据备份', key: STORAGE_KEYS.DATA_BACKUP_LIST },
]

const moduleList = ref<ModuleInfo[]>([])

const storageStats = computed(() => {
  const totalSize = moduleList.value.reduce((s, m) => s + m.size, 0)
  const totalRecords = moduleList.value.reduce((s, m) => s + m.count, 0)
  const usedModules = moduleList.value.filter(m => m.count > 0).length
  const percent = +Math.min(100, (totalSize / STORAGE_LIMIT) * 100).toFixed(2)
  return { totalSize, totalRecords, usedModules, percent }
})

const progressColor = computed(() => {
  const p = storageStats.value.percent
  if (p < 50) return '#36D399'
  if (p < 80) return '#FF9500'
  return '#F53F3F'
})

function refreshStorage() {
  moduleList.value = moduleDefinitions.map(m => {
    const raw = localStorage.getItem(PREFIX + m.key)
    let count = 0
    let size = 0
    if (raw) {
      size = new Blob([raw]).size
      try {
        const parsed = JSON.parse(raw)
        count = Array.isArray(parsed) ? parsed.length : Object.keys(parsed).length
      } catch { /* ignore */ }
    }
    return { name: m.name, key: m.key, count, size }
  })
}

// 清理缓存时保留的必要数据
const ESSENTIAL_KEYS = new Set<string>([
  STORAGE_KEYS.ADMIN_ACCOUNT_LIST,
  STORAGE_KEYS.SITE_CONFIG,
  STORAGE_KEYS.SYSTEM_SETTINGS,
  STORAGE_KEYS.OSS_CONFIG,
  STORAGE_KEYS.VOD_CONFIG,
  STORAGE_KEYS.POINTS_CONFIG,
  STORAGE_KEYS.NOTIFY_CONFIG,
])

function handleClearCache() {
  ElMessageBox.confirm('确定清理缓存吗？将清除非必要数据（保留管理员账号和系统设置）。', '清理缓存', {
    type: 'warning',
    confirmButtonText: '确认清理',
  })
    .then(() => {
      let cleared = 0
      for (const m of moduleList.value) {
        if (!ESSENTIAL_KEYS.has(m.key) && m.count > 0) {
          removeLocalData(m.key)
          cleared++
        }
      }
      refreshStorage()
      refreshFeatures()
      ElMessage.success(`已清理 ${cleared} 个模块的缓存数据`)
      opLog.logDelete('清理系统缓存数据')
    })
    .catch(() => {})
}

// ===== Tab 3: 功能清单 =====
interface FeatureItem {
  name: string
  route: string
  storageKey: string
  logModule: string
  count: number
  status: 'normal' | 'unused'
  lastOperTime: string
}

/** 后台模块清单：模块名称、路由路径、存储键、日志模块 */
const featureDefinitions: { name: string; route: string; storageKey: string; logModule: string }[] = [
  { name: '学员管理', route: '/admin/student', storageKey: STORAGE_KEYS.STUDENT_LIST, logModule: 'student' },
  { name: '教师管理', route: '/admin/teacher', storageKey: STORAGE_KEYS.TEACHER_LIST, logModule: 'teacher' },
  { name: '注册码管理', route: '/admin/regcode', storageKey: STORAGE_KEYS.REG_CODE_LIST, logModule: 'regcode' },
  { name: '课程管理', route: '/admin/course', storageKey: STORAGE_KEYS.COURSE_LIST, logModule: 'course' },
  { name: '试听预约', route: '/admin/trial', storageKey: STORAGE_KEYS.TRIAL_LIST, logModule: 'trial' },
  { name: '赛事管理', route: '/admin/exam', storageKey: STORAGE_KEYS.EXAM_LIST, logModule: 'exam' },
  { name: 'Banner管理', route: '/admin/banner', storageKey: STORAGE_KEYS.BANNER_LIST, logModule: 'banner' },
  { name: '报名管理', route: '/admin/registration', storageKey: STORAGE_KEYS.REGISTRATION_LIST, logModule: 'registration' },
  { name: '题库中心', route: '/admin/question-bank', storageKey: STORAGE_KEYS.QUESTION_LIST, logModule: 'question' },
  { name: '试卷中心', route: '/admin/paper', storageKey: STORAGE_KEYS.PAPER_LIST, logModule: 'paper' },
  { name: '成绩管理', route: '/admin/score', storageKey: STORAGE_KEYS.SCORE_LIST, logModule: 'score' },
  { name: '班级管理', route: '/admin/class', storageKey: STORAGE_KEYS.CLASS_LIST, logModule: 'class' },
  { name: '营销管理', route: '/admin/marketing', storageKey: STORAGE_KEYS.MARKETING, logModule: 'marketing' },
  { name: '文件管理', route: '/admin/file', storageKey: STORAGE_KEYS.FILE_LIST, logModule: 'file' },
  { name: '系统设置', route: '/admin/settings', storageKey: STORAGE_KEYS.SITE_CONFIG, logModule: 'system' },
  { name: '资讯管理', route: '/admin/news', storageKey: STORAGE_KEYS.NEWS_LIST, logModule: 'news' },
  { name: '分类管理', route: '/admin/category', storageKey: STORAGE_KEYS.CATEGORIES, logModule: 'category' },
  { name: '合作伙伴', route: '/admin/partner', storageKey: STORAGE_KEYS.PARTNERS, logModule: 'partner' },
  { name: '首页配置', route: '/admin/homepage-config', storageKey: STORAGE_KEYS.HOMEPAGE_CONFIG, logModule: 'homepage' },
  { name: '关于我们配置', route: '/admin/about-config', storageKey: STORAGE_KEYS.ABOUT_CONFIG, logModule: 'about' },
  { name: '云存储配置', route: '/admin/storage-config', storageKey: STORAGE_KEYS.OSS_CONFIG, logModule: 'storage' },
  { name: '通知管理', route: '/admin/notification', storageKey: STORAGE_KEYS.NOTIFICATION_LIST, logModule: 'notification' },
  { name: '数据备份', route: '/admin/data-backup', storageKey: STORAGE_KEYS.DATA_BACKUP_LIST, logModule: 'backup' },
]

const featureList = ref<FeatureItem[]>([])

function getModuleCount(key: string): number {
  const raw = localStorage.getItem(PREFIX + key)
  if (!raw) return 0
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.length : Object.keys(parsed).length
  } catch { return 0 }
}

function getLastOperTime(logModule: string): string {
  const found = logList.value.find(l => l.module === logModule)
  return found ? found.operTime : '-'
}

function refreshFeatures() {
  featureList.value = featureDefinitions.map(f => {
    const count = getModuleCount(f.storageKey)
    return {
      name: f.name,
      route: f.route,
      storageKey: f.storageKey,
      logModule: f.logModule,
      count,
      status: count > 0 ? 'normal' as const : 'unused' as const,
      lastOperTime: getLastOperTime(f.logModule),
    }
  })
}

// ===== Tab 4: 系统日志摘要 =====
const logList = ref<OperationLogEntry[]>([])

function refreshLogs() {
  logList.value = getLocalData<OperationLogEntry[]>(STORAGE_KEYS.OPERATION_LOG_LIST, [])
}

const logStats = computed(() => {
  const total = logList.value.length
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  const todayCount = logList.value.filter(l => l.operTime.startsWith(todayStr)).length
  const successCount = logList.value.filter(l => l.status === 'success').length
  const successRate = total > 0 ? ((successCount / total) * 100).toFixed(1) : '0.0'
  const mostActive = moduleDist.value[0]?.label || '-'
  return { total, today: todayCount, successRate, mostActive }
})

const moduleDist = computed(() => {
  const map: Record<string, number> = {}
  for (const log of logList.value) {
    map[log.module] = (map[log.module] || 0) + 1
  }
  const total = logList.value.length || 1
  return Object.entries(map)
    .map(([mod, count]) => ({
      module: mod,
      label: moduleLabelMap[mod] || mod,
      count,
      percent: +((count / total) * 100).toFixed(1),
    }))
    .sort((a, b) => b.count - a.count)
})

const recentLogs = computed(() => logList.value.slice(0, 10))

// ===== 通用 =====
function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + ' MB'
  return (bytes / 1073741824).toFixed(2) + ' GB'
}

function refreshAll() {
  refreshSystemInfo()
  refreshStorage()
  refreshLogs()
  refreshFeatures()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  refreshSystemInfo()
  refreshLogs()
  refreshStorage()
  refreshFeatures()
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)
})

onUnmounted(() => {
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.system-monitor-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;
  h2 { font-size: 20px; color: #1D2129; } }
.page-desc { color: #666; font-size: 13px; margin-bottom: 16px; }
.monitor-tabs { margin-top: 8px; }

.stat-cards { display: flex; gap: 16px; margin-bottom: 20px; flex-wrap: wrap;
  .stat-card { flex: 1; min-width: 150px; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 24px; font-weight: 600; color: #1D2129; margin-bottom: 4px; word-break: break-all; }
    .stat-label { font-size: 13px; color: #666; }
  }
}

.online-card .stat-value { display: flex; align-items: center; justify-content: center; gap: 6px; }
.online { color: #36D399; }
.offline { color: #F53F3F; }
.dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.dot-online { background: #36D399; box-shadow: 0 0 6px rgba(103, 194, 58, 0.6); }
.dot-offline { background: #F53F3F; }

.action-bar { display: flex; align-items: center; gap: 16px; margin-top: 8px;
  .update-time { color: #999; font-size: 13px; } }

.module-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; color: #1D2129;
  .usage-text { font-size: 13px; font-weight: 400; color: #666; } }

.log-layout { display: flex; gap: 20px; align-items: flex-start;
  .dist-card { flex: 1; }
  .recent-card { flex: 1.4; } }

.dist-list { display: flex; flex-direction: column; gap: 12px; }
.dist-item { display: flex; align-items: center; gap: 10px; font-size: 13px;
  .dist-label { width: 90px; color: $text-primary; flex-shrink: 0; }
  .dist-bar-wrap { flex: 1; height: 14px; background: #F7F8FA; border-radius: 7px; overflow: hidden; }
  .dist-bar { height: 100%; background: linear-gradient(90deg, #165DFF, #4080FF); border-radius: 7px; transition: width 0.4s ease; min-width: 2px; }
  .dist-count { width: 36px; text-align: right; color: #666; flex-shrink: 0; } }

.empty-tip { text-align: center; color: #999; padding: 30px 0; font-size: 13px; }
</style>
