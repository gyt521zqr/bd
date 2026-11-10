<template>
  <!-- 操作日志管理 -->
  <div class="operation-log-page">
    <div class="page-header">
      <h2>操作日志</h2>
      <div class="header-actions">
        <el-button @click="handleExport">导出日志</el-button>
        <el-button type="danger" @click="handleClearLog" v-if="filteredList.length > 0">清空日志</el-button>
      </div>
    </div>

    <p class="page-desc">记录后台管理操作行为，便于审计追踪。</p>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">日志总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.success }}</div>
        <div class="stat-label">成功操作</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #F53F3F;">{{ stats.fail }}</div>
        <div class="stat-label">失败操作</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #165DFF;">{{ stats.today }}</div>
        <div class="stat-label">今日操作</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索操作内容" clearable style="width: 240px" />
      <el-select v-model="moduleFilter" placeholder="操作模块" clearable style="width: 160px">
        <el-option label="学员管理" value="student" />
        <el-option label="教师管理" value="teacher" />
        <el-option label="课程管理" value="course" />
        <el-option label="赛事管理" value="exam" />
        <el-option label="题库管理" value="question" />
        <el-option label="系统设置" value="system" />
        <el-option label="报名管理" value="registration" />
        <el-option label="成绩管理" value="score" />
        <el-option label="试卷管理" value="paper" />
        <el-option label="班级管理" value="class" />
        <el-option label="新闻资讯" value="news" />
        <el-option label="轮播图" value="banner" />
        <el-option label="文件管理" value="file" />
        <el-option label="通知公告" value="notification" />
        <el-option label="注册码管理" value="regcode" />
        <el-option label="练习管理" value="practice" />
        <el-option label="营销管理" value="marketing" />
        <el-option label="合作伙伴" value="partner" />
        <el-option label="分类管理" value="category" />
        <el-option label="首页管理" value="homepage" />
        <el-option label="关于我们" value="about" />
        <el-option label="存储管理" value="storage" />
        <el-option label="备份管理" value="backup" />
        <el-option label="试用管理" value="trial" />
      </el-select>
      <el-select v-model="actionFilter" placeholder="操作类型" clearable style="width: 120px">
        <el-option label="新增" value="create" />
        <el-option label="编辑" value="update" />
        <el-option label="删除" value="delete" />
        <el-option label="登录" value="login" />
        <el-option label="导出" value="export" />
        <el-option label="导入" value="import" />
        <el-option label="配置" value="config" />
        <el-option label="批量删除" value="batch_delete" />
        <el-option label="状态变更" value="status_change" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 100px">
        <el-option label="成功" value="success" />
        <el-option label="失败" value="fail" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 日志列表 -->
    <el-table :data="pagedList" stripe v-loading="loading" empty-text="暂无数据">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="module" label="模块" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="moduleTagMap[row.module]?.type">{{ moduleTagMap[row.module]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="action" label="操作" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="actionTagType(row.action)">{{ actionMap[row.action] || row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="操作内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="130" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
            {{ row.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operTime" label="操作时间" width="170" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row as LogItem)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="filteredList.length" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="日志详情" width="600px">
      <div v-if="viewRow" class="view-body">
        <p><strong>操作人：</strong>{{ viewRow.operator }}</p>
        <p><strong>模块：</strong>{{ moduleTagMap[viewRow.module]?.label }}</p>
        <p><strong>操作类型：</strong>{{ actionMap[viewRow.action] }}</p>
        <p><strong>操作内容：</strong>{{ viewRow.description }}</p>
        <p><strong>IP地址：</strong>{{ viewRow.ip }}</p>
        <p><strong>状态：</strong><el-tag :type="viewRow.status === 'success' ? 'success' : 'danger'" size="small">{{ viewRow.status === 'success' ? '成功' : '失败' }}</el-tag></p>
        <p><strong>操作时间：</strong>{{ viewRow.operTime }}</p>
        <div v-if="viewRow.detail">
          <p><strong>详细信息：</strong></p>
          <div class="content-box">{{ viewRow.detail }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * OperationLogPage - 操作日志管理
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'

const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const keyword = ref('')
const moduleFilter = ref('')
const actionFilter = ref('')
const statusFilter = ref('')

interface LogItem {
  id: number
  operator: string
  module: string
  action: string
  description: string
  ip: string
  status: string
  operTime: string
  detail?: string
}

const moduleTagMap: Record<string, { label: string; type: 'primary' | 'success' | 'warning' | 'info' | 'danger' }> = {
  student: { label: '学员管理', type: 'primary' },
  teacher: { label: '教师管理', type: 'success' },
  course: { label: '课程管理', type: 'warning' },
  exam: { label: '赛事管理', type: 'danger' },
  question: { label: '题库管理', type: 'info' },
  system: { label: '系统设置', type: 'primary' },
  registration: { label: '报名管理', type: 'info' },
  score: { label: '成绩管理', type: 'success' },
  paper: { label: '试卷管理', type: 'warning' },
  class: { label: '班级管理', type: 'primary' },
  news: { label: '新闻资讯', type: 'info' },
  banner: { label: '轮播图', type: 'warning' },
  file: { label: '文件管理', type: 'primary' },
  notification: { label: '通知公告', type: 'success' },
  regcode: { label: '注册码管理', type: 'info' },
  practice: { label: '练习管理', type: 'warning' },
  marketing: { label: '营销管理', type: 'danger' },
  partner: { label: '合作伙伴', type: 'primary' },
  category: { label: '分类管理', type: 'info' },
  homepage: { label: '首页管理', type: 'primary' },
  about: { label: '关于我们', type: 'info' },
  storage: { label: '存储管理', type: 'warning' },
  backup: { label: '备份管理', type: 'danger' },
  trial: { label: '试用管理', type: 'info' },
}

const actionMap: Record<string, string> = {
  create: '新增',
  update: '编辑',
  delete: '删除',
  login: '登录',
  export: '导出',
  import: '导入',
  config: '配置',
  batch_delete: '批量删除',
  status_change: '状态变更',
}

function actionTagType(action: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    create: 'success',
    update: 'primary',
    delete: 'danger',
    login: 'info',
    export: 'warning',
    import: 'warning',
    config: 'primary',
    batch_delete: 'danger',
    status_change: 'info',
  }
  return map[action] || 'info'
}

const logList = ref<LogItem[]>([])

function saveLogToLocal() {
  setLocalData(STORAGE_KEYS.OPERATION_LOG_LIST, logList.value)
}

const stats = computed(() => {
  const total = logList.value.length
  const success = logList.value.filter(l => l.status === 'success').length
  const fail = logList.value.filter(l => l.status === 'fail').length
  const today = new Date().toISOString().slice(0, 10)
  const todayCount = logList.value.filter(l => l.operTime.includes(today.replace(/-/g, '-').slice(5))).length
  return { total, success, fail, today: todayCount }
})

const filteredList = computed(() => {
  return logList.value.filter(item => {
    const matchKeyword = !keyword.value || item.description.includes(keyword.value) || item.operator.includes(keyword.value)
    const matchModule = !moduleFilter.value || item.module === moduleFilter.value
    const matchAction = !actionFilter.value || item.action === actionFilter.value
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchModule && matchAction && matchStatus
  })
})

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function handleSearch() { pageNum.value = 1 }

function handleView(row: LogItem) {
  viewRow.value = row
  viewDialogVisible.value = true
}

function handleExport() {
  const headers = ['ID', '操作人', '模块', '操作类型', '操作内容', 'IP', '状态', '时间']
  const rows = filteredList.value.map(l => [l.id, l.operator, moduleTagMap[l.module]?.label, actionMap[l.action], l.description, l.ip, l.status === 'success' ? '成功' : '失败', l.operTime])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `操作日志_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

function handleClearLog() {
  ElMessageBox.confirm('确定清空所有操作日志吗？此操作不可恢复！', '危险操作', { type: 'error', confirmButtonText: '确认清空' })
    .then(() => {
      logList.value = []
      saveLogToLocal()
      ElMessage.success('日志已清空')
    })
    .catch(() => {})
}

const viewDialogVisible = ref(false)
const viewRow = ref<LogItem | null>(null)

onMounted(() => {
  const localData = getLocalData<LogItem[] | null>(STORAGE_KEYS.OPERATION_LOG_LIST, null)
  if (localData && localData.length > 0) {
    logList.value = localData
  } else {
    saveLogToLocal()
  }
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.operation-log-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; h2 { font-size: 20px; color: #1D2129; } .header-actions { display: flex; gap: 8px; } }
.page-desc { color: #666; font-size: 13px; margin-bottom: 16px; }
.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.view-body { line-height: 1.8; color: $text-primary; p { margin-bottom: 8px; } }
.content-box { background: #f5f7fa; border-radius: 8px; padding: 12px 16px; margin: 8px 0 16px; white-space: pre-wrap; word-break: break-all; font-family: monospace; font-size: 13px; }
</style>
