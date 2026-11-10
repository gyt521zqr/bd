<template>
  <!-- 通知消息管理 -->
  <div class="notification-manage-page">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="handleCreate">发送通知</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">通知总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #165DFF;">{{ stats.unread }}</div>
        <div class="stat-label">未读</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.read }}</div>
        <div class="stat-label">已读</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #FF9500;">{{ stats.today }}</div>
        <div class="stat-label">今日发送</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索通知标题" clearable style="width: 240px" />
      <el-select v-model="typeFilter" placeholder="通知类型" clearable style="width: 140px">
        <el-option label="系统通知" value="system" />
        <el-option label="赛事通知" value="exam" />
        <el-option label="课程通知" value="course" />
        <el-option label="活动通知" value="activity" />
      </el-select>
      <el-select v-model="targetFilter" placeholder="发送对象" clearable style="width: 140px">
        <el-option label="全体用户" value="all" />
        <el-option label="学员" value="student" />
        <el-option label="教师" value="teacher" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 通知列表 -->
    <el-table :data="pagedList" stripe v-loading="loading" empty-text="暂无数据" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="通知标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTagMap[row.type]?.type">{{ typeTagMap[row.type]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="target" label="发送对象" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ targetMap[row.target] || row.target }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="readCount" label="阅读数" width="80" align="center" />
      <el-table-column prop="totalCount" label="接收数" width="80" align="center" />
      <el-table-column prop="sendTime" label="发送时间" width="170" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'sent' ? 'success' : 'warning'" size="small">
            {{ row.status === 'sent' ? '已发送' : '待发送' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row as NotificationItem)">查看</el-button>
          <el-button size="small" text type="warning" @click="handleResend(row as NotificationItem)">重发</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row as NotificationItem)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="filteredList.length" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 发送通知弹窗 -->
    <el-dialog v-model="dialogVisible" title="发送通知" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="form.type" placeholder="选择通知类型" style="width: 100%">
            <el-option label="系统通知" value="system" />
            <el-option label="赛事通知" value="exam" />
            <el-option label="课程通知" value="course" />
            <el-option label="活动通知" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送对象" prop="target">
          <el-select v-model="form.target" placeholder="选择发送对象" style="width: 100%">
            <el-option label="全体用户" value="all" />
            <el-option label="学员" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入通知内容" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="立即发送" prop="immediate">
          <el-switch v-model="form.immediate" />
          <span class="form-tip">关闭后将保存为草稿，可稍后发送</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="通知详情" width="600px">
      <div v-if="viewRow" class="view-body">
        <p><strong>标题：</strong>{{ viewRow.title }}</p>
        <p><strong>类型：</strong>{{ typeTagMap[viewRow.type]?.label }}</p>
        <p><strong>发送对象：</strong>{{ targetMap[viewRow.target] || viewRow.target }}</p>
        <p><strong>内容：</strong></p>
        <div class="content-box">{{ viewRow.content }}</div>
        <p><strong>发送时间：</strong>{{ viewRow.sendTime }}</p>
        <p><strong>阅读率：</strong>{{ viewRow.readCount }} / {{ viewRow.totalCount }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * NotificationManagePage - 通知消息管理
 */
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { setLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { notificationApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('notification')

const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const keyword = ref('')
const typeFilter = ref('')
const targetFilter = ref('')

interface NotificationItem {
  id: number
  title: string
  type: string
  target: string
  content: string
  readCount: number
  totalCount: number
  sendTime: string
  status: string
}

const typeTagMap: Record<string, { label: string; type: 'primary' | 'success' | 'warning' | 'info' | 'danger' }> = {
  system: { label: '系统通知', type: 'primary' },
  exam: { label: '赛事通知', type: 'warning' },
  course: { label: '课程通知', type: 'success' },
  activity: { label: '活动通知', type: 'danger' },
}

const targetMap: Record<string, string> = {
  all: '全体用户',
  student: '学员',
  teacher: '教师',
}

const notifyList = ref<NotificationItem[]>([])

function saveNotifyToLocal() {
  setLocalData(STORAGE_KEYS.NOTIFICATION_LIST, notifyList.value)
}

const stats = computed(() => {
  const total = notifyList.value.length
  const unread = notifyList.value.reduce((sum, n) => sum + (n.totalCount - n.readCount), 0)
  const read = notifyList.value.reduce((sum, n) => sum + n.readCount, 0)
  const today = new Date().toISOString().slice(0, 10)
  const todayCount = notifyList.value.filter(n => n.sendTime.startsWith(today.replace(/-/g, '-'))).length
  return { total, unread, read, today: todayCount }
})

const filteredList = computed(() => {
  return notifyList.value.filter(item => {
    const matchKeyword = !keyword.value || item.title.includes(keyword.value)
    const matchType = !typeFilter.value || item.type === typeFilter.value
    const matchTarget = !targetFilter.value || item.target === targetFilter.value
    return matchKeyword && matchType && matchTarget
  })
})

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function handleSearch() { pageNum.value = 1 }
function handleView(row: NotificationItem) {
  viewRow.value = row
  viewDialogVisible.value = true
}
function handleResend(row: NotificationItem) {
  ElMessageBox.confirm(`确定重新发送通知 "${row.title}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await notificationApi.update(row.id, { ...row, status: 'sent', readCount: 0 })
        row.status = 'sent'
        row.readCount = 0
        saveNotifyToLocal()
        ElMessage.success('已重新发送')
      } catch {
        ElMessage.error('重发失败')
      }
    })
    .catch(() => {})
}
function handleDelete(row: NotificationItem) {
  ElMessageBox.confirm(`确定删除通知 "${row.title}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await notificationApi.remove(row.id)
        notifyList.value = notifyList.value.filter(n => n.id !== row.id)
        saveNotifyToLocal()
        ElMessage.success('删除成功')
        opLog.logDelete('删除通知')
      } catch {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 发送通知弹窗
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  title: '',
  type: 'system',
  target: 'all',
  content: '',
  immediate: true,
})
const formRules: FormRules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  target: [{ required: true, message: '请选择发送对象', trigger: 'change' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
}

function handleCreate() {
  Object.assign(form, { title: '', type: 'system', target: 'all', content: '', immediate: true })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        title: form.title,
        type: form.type,
        target: form.target,
        content: form.content,
        status: form.immediate ? 'sent' : 'pending',
      }
      const res: any = await notificationApi.create(payload)
      const data: any = res?.data ?? {}
      const newId = data.id ?? (notifyList.value.length > 0 ? Math.max(...notifyList.value.map(n => n.id)) + 1 : 1)
      notifyList.value.unshift({
        id: newId,
        title: form.title,
        type: form.type,
        target: form.target,
        content: form.content,
        readCount: data.readCount ?? 0,
        totalCount: data.totalCount ?? 0,
        sendTime: data.sendTime || data.createTime || '',
        status: data.status || payload.status,
      })
      saveNotifyToLocal()
      ElMessage.success(form.immediate ? '通知已发送' : '通知已保存为草稿')
      opLog.logCreate('发送通知')
      dialogVisible.value = false
    } catch {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 查看详情
const viewDialogVisible = ref(false)
const viewRow = ref<NotificationItem | null>(null)

// ==================== 批量操作与导出 ====================
const selectedIds = ref<number[]>([])

function handleSelectionChange(rows: NotificationItem[]) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 条通知吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await notificationApi.batchRemove(selectedIds.value)
        notifyList.value = notifyList.value.filter(n => !selectedIds.value.includes(n.id))
        selectedIds.value = []
        saveNotifyToLocal()
        ElMessage.success('批量删除成功')
      } catch {
        ElMessage.error('批量删除失败')
      }
    })
    .catch(() => {})
}

function handleExport() {
  const headers = ['ID', '标题', '类型', '发送对象', '阅读数', '接收数', '发送时间', '状态']
  const rows = filteredList.value.map(n => [n.id, n.title, typeTagMap[n.type]?.label || n.type, targetMap[n.target] || n.target, n.readCount, n.totalCount, n.sendTime, n.status === 'sent' ? '已发送' : '待发送'])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `通知数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

async function loadNotifications() {
  loading.value = true
  try {
    const res: any = await notificationApi.list({
      pageNum: pageNum.value,
      pageSize: 100,
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined,
    })
    const list = res?.data?.list
    if (res?.code === 200 && Array.isArray(list)) {
      notifyList.value = list.map((item: any) => ({
        ...item,
        readCount: item.readCount ?? 0,
        totalCount: item.totalCount ?? 0,
        sendTime: item.sendTime || item.createTime || '',
        target: item.target || 'all',
      }))
    } else {
      notifyList.value = []
      ElMessage.error('数据加载失败，请检查网络或稍后重试')
    }
  } catch {
    notifyList.value = []
    ElMessage.error('数据加载失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.notification-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }
.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.form-tip { font-size: 12px; color: #86909C; margin-left: 8px; }
.view-body { line-height: 1.8; color: $text-primary; p { margin-bottom: 8px; } }
.content-box { background: #f5f7fa; border-radius: 8px; padding: 12px 16px; margin: 8px 0 16px; white-space: pre-wrap; word-break: break-all; }
</style>
