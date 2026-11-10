<template>
  <!-- 试听预约管理 -->
  <div class="trial-manage-page">
    <div class="page-header">
      <h2>试听预约管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总预约数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #FF9500;">{{ stats.pending }}</div>
        <div class="stat-label">待处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #165DFF;">{{ stats.confirmed }}</div>
        <div class="stat-label">已确认</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.completed }}</div>
        <div class="stat-label">已完成</div>
      </div>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索学生姓名/家长电话" clearable style="width: 240px" @input="handleFilterChange" @clear="handleFilterChange" />
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="handleFilterChange">
        <el-option label="待处理" value="pending" />
        <el-option label="已确认" value="confirmed" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <el-table :data="pagedList" stripe v-loading="loading" empty-text="暂无数据" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="studentName" label="学生姓名" width="100" />
      <el-table-column prop="parentName" label="家长姓名" width="100" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="courseName" label="预约课程" min-width="150" />
      <el-table-column prop="age" label="年龄" width="60" />
      <el-table-column prop="expectTime" label="期望时间" width="150" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="预约时间" width="150" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row)">详情</el-button>
          <el-button size="small" text type="primary" @click="handleConfirm(row)" :disabled="row.status !== 'pending'">确认</el-button>
          <el-button size="small" text type="success" @click="handleComplete(row)" :disabled="row.status !== 'confirmed'">完成</el-button>
          <el-button size="small" text type="danger" @click="handleCancel(row)" :disabled="row.status === 'cancelled' || row.status === 'completed'">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <el-dialog v-model="viewDialogVisible" title="预约详情" width="560px">
      <div v-if="viewRow" class="view-body" style="line-height: 2;">
        <p><strong>预约ID：</strong>{{ viewRow.id }}</p>
        <p><strong>学生姓名：</strong>{{ viewRow.studentName }}</p>
        <p><strong>家长姓名：</strong>{{ viewRow.parentName }}</p>
        <p><strong>联系电话：</strong>{{ viewRow.phone }}</p>
        <p><strong>预约课程：</strong>{{ viewRow.courseName }}</p>
        <p><strong>学生年龄：</strong>{{ viewRow.age }} 岁</p>
        <p><strong>期望时间：</strong>{{ viewRow.expectTime }}</p>
        <p><strong>当前状态：</strong>{{ statusLabel(viewRow.status) }}</p>
        <p><strong>预约时间：</strong>{{ viewRow.createTime }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * TrialManagePage - 试听预约管理
 * 搜索、筛选、分页、状态变更、API集成 + localStorage持久化
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import request from '@/api'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('trial')

// ==================== 搜索与筛选 ====================
const keyword = ref('')
const statusFilter = ref('')
const loading = ref(false)

// ==================== 分页 ====================
const pageNum = ref(1)
const pageSize = ref(20)

// ==================== 数据 ====================
const trialList = ref<any[]>([])

// ==================== 统计 ====================
const stats = computed(() => {
  const total = trialList.value.length
  const pending = trialList.value.filter(t => t.status === 'pending').length
  const confirmed = trialList.value.filter(t => t.status === 'confirmed').length
  const completed = trialList.value.filter(t => t.status === 'completed').length
  return { total, pending, confirmed, completed }
})

// ==================== 过滤与分页 ====================
const filteredList = computed(() => {
  return trialList.value.filter((item) => {
    const kw = keyword.value.trim()
    const matchKeyword = !kw || item.studentName.includes(kw) || item.phone.includes(kw)
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchStatus
  })
})

const total = computed(() => filteredList.value.length)

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// ==================== 工具函数 ====================
function statusType(status: string): 'info' | 'primary' | 'success' | 'warning' | 'danger' {
  const map: Record<string, 'info' | 'primary' | 'success' | 'warning' | 'danger'> = { pending: 'warning', confirmed: 'primary', completed: 'success', cancelled: 'info' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { pending: '待处理', confirmed: '已确认', completed: '已完成', cancelled: '已取消' }
  return map[status] || status
}

// ==================== localStorage 持久化 ====================
function saveToLocal() {
  setLocalData(STORAGE_KEYS.TRIAL_LIST, trialList.value)
}

// ==================== 数据加载 ====================
async function fetchList() {
  loading.value = true
  try {
    const res = await request.get('/admin/trial/list', {
      params: {
        keyword: keyword.value,
        status: statusFilter.value,
        pageNum: pageNum.value,
        pageSize: pageSize.value,
      },
      silent: true,
    }) as any
    if (res?.data) {
      const list = res.data.list || res.data.records || []
      if (list.length > 0) {
        trialList.value = list
      }
    }
  } catch {
    // API 不可用，保留现有数据
  } finally {
    loading.value = false
  }
}

// ==================== 搜索 ====================
function handleSearch() {
  pageNum.value = 1
  fetchList()
}

function handleFilterChange() {
  pageNum.value = 1
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = ''
  pageNum.value = 1
  fetchList()
}

// ==================== 状态变更 ====================
async function handleConfirm(row: any) {
  try {
    await request.put(`/admin/trial/${row.id}/confirm`, {}, { silent: true })
  } catch {
    // API 不可用
  }
  row.status = 'confirmed'
  ElMessage.success('已确认')
  opLog.logStatusChange('更新试听状态')
  saveToLocal()
}

async function handleComplete(row: any) {
  try {
    await request.put(`/admin/trial/${row.id}/complete`, {}, { silent: true })
  } catch {
    // API 不可用
  }
  row.status = 'completed'
  ElMessage.success('已标记完成')
  opLog.logStatusChange('更新试听状态')
  saveToLocal()
}

function handleCancel(row: any) {
  ElMessageBox.confirm(`确定取消 "${row.studentName}" 的试听预约吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await request.put(`/admin/trial/${row.id}/cancel`, {}, { silent: true })
      } catch {
        // API 不可用
      }
      row.status = 'cancelled'
      ElMessage.success('已取消')
      opLog.logStatusChange('更新试听状态')
      saveToLocal()
    })
    .catch(() => {})
}

// ==================== 批量操作、导出与详情 ====================
const selectedIds = ref<number[]>([])

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 条预约记录吗？`, '警告', { type: 'warning' })
    .then(() => {
      trialList.value = trialList.value.filter((t: any) => !selectedIds.value.includes(t.id))
      selectedIds.value = []
      saveToLocal()
      ElMessage.success('批量删除成功')
      opLog.logDelete('删除试听记录')
    })
    .catch(() => {})
}

function handleExport() {
  const headers = ['ID', '学生姓名', '家长姓名', '联系电话', '预约课程', '年龄', '期望时间', '状态', '预约时间']
  const rows = filteredList.value.map((t: any) => [t.id, t.studentName, t.parentName, t.phone, t.courseName, t.age, t.expectTime, statusLabel(t.status), t.createTime])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `试听预约_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const viewDialogVisible = ref(false)
const viewRow = ref<any>(null)

function handleView(row: any) {
  viewRow.value = row
  viewDialogVisible.value = true
}

// ==================== 初始化 ====================
onMounted(() => {
  const localData = getLocalData<any[]>(STORAGE_KEYS.TRIAL_LIST)
  if (localData && localData.length > 0) {
    trialList.value = localData
  }
  fetchList()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.trial-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }

.stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;

  .stat-card {
    flex: 1;
    background: $bg-white;
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    padding: 16px;
    text-align: center;

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #1D2129;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 13px;
      color: #666;
    }
  }
}

.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
