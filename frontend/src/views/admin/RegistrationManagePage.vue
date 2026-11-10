<template>
  <!-- 赛事报名管理 -->
  <div class="registration-manage-page">
    <div class="page-header">
      <h2>报名管理</h2>
      <el-button type="primary" @click="handleExport">导出名单</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总报名数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #FF9500;">{{ stats.registered }}</div>
        <div class="stat-label">待审核</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.approved }}</div>
        <div class="stat-label">已审核</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #86909C;">{{ stats.cancelled }}</div>
        <div class="stat-label">已取消</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索赛事名称/学生姓名/学校" clearable style="width: 280px" />
      <el-select v-model="statusFilter" placeholder="报名状态" clearable style="width: 120px">
        <el-option label="已报名" value="registered" />
        <el-option label="已取消" value="cancelled" />
        <el-option label="已审核" value="approved" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="success" @click="handleBatchApprove">批量审核通过</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchReject">批量驳回</el-button>
    </div>

    <el-table :data="pagedList" stripe @selection-change="handleSelectionChange" empty-text="暂无数据">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="registerNo" label="报名号" width="150" />
      <el-table-column prop="studentName" label="学生姓名" width="100" />
      <el-table-column prop="school" label="学校" min-width="160" />
      <el-table-column prop="grade" label="年级" width="80" />
      <el-table-column prop="examName" label="参赛赛事" min-width="180" />
      <el-table-column prop="status" label="报名状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="registerTime" label="报名时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'registered'" size="small" text type="success" @click="handleApprove(row)">审核通过</el-button>
          <el-button v-if="row.status === 'registered'" size="small" text type="danger" @click="handleReject(row)">驳回</el-button>
          <el-button size="small" text type="primary" @click="handleViewDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="报名详情" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="报名号">{{ detailRow.registerNo }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detailRow.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ detailRow.school }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ detailRow.grade }}</el-descriptions-item>
        <el-descriptions-item label="参赛赛事">{{ detailRow.examName }}</el-descriptions-item>
        <el-descriptions-item label="报名状态">
          <el-tag :type="statusTagType(detailRow.status)" size="small">{{ statusLabel(detailRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报名时间">{{ detailRow.registerTime }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailRow.phone }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * RegistrationManagePage - 赛事报名管理
 * 报名记录列表、搜索、筛选、审核、导出
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { registrationApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('registration')

const pageNum = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const statusFilter = ref('')
const selectedIds = ref<number[]>([])

const registrationList = ref<any[]>([])

// 按搜索条件筛选后的列表
const filteredList = computed(() => {
  return registrationList.value.filter((item: any) => {
    const kw = keyword.value.toLowerCase()
    const matchKeyword = !kw || 
      item.studentName?.toLowerCase().includes(kw) || 
      item.school?.toLowerCase().includes(kw) || 
      item.examName?.toLowerCase().includes(kw)
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchStatus
  })
})

// 当前页展示数据（分页切片）
const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// 总条数
const total = computed(() => filteredList.value.length)

const stats = computed(() => {
  const total = registrationList.value.length
  const registered = registrationList.value.filter(r => r.status === 'registered').length
  const approved = registrationList.value.filter(r => r.status === 'approved').length
  const cancelled = registrationList.value.filter(r => r.status === 'cancelled').length
  return { total, registered, approved, cancelled }
})

const detailDialogVisible = ref(false)
const detailRow = ref<any>({})

function statusTagType(status: string): 'info' | 'primary' | 'success' | 'warning' | 'danger' {
  const map: Record<string, 'info' | 'primary' | 'success' | 'warning' | 'danger'> = { registered: 'warning', approved: 'success', cancelled: 'info' }
  return map[status] || 'info'
}
function statusLabel(status: string) {
  const map: Record<string, string> = { registered: '已报名', approved: '已审核', cancelled: '已取消' }
  return map[status] || status
}

/** 搜索/获取报名列表，调用真实 API，失败时清空列表并提示 */
async function handleSearch() {
  try {
    const res = await registrationApi.list({ pageNum: 1, pageSize: 9999 })
    const data: any = res?.data
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
    if (res?.code === 200 && Array.isArray(list)) {
      registrationList.value = list
    } else {
      registrationList.value = []
      ElMessage.error('数据加载失败，请检查网络或稍后重试')
    }
  } catch {
    registrationList.value = []
    ElMessage.error('数据加载失败，请检查网络或稍后重试')
  }
}
function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}
function handleApprove(row: any) {
  ElMessageBox.confirm(`确定审核通过 "${row.studentName}" 的报名吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await registrationApi.update(row.id, { status: 'approved' })
        row.status = 'approved'
        ElMessage.success('审核通过')
        opLog.logStatusChange('审核通过报名')
      } catch {
        ElMessage.error('审核失败')
      }
    })
    .catch(() => {})
}
function handleReject(row: any) {
  ElMessageBox.confirm(`确定驳回 "${row.studentName}" 的报名吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await registrationApi.update(row.id, { status: 'cancelled' })
        row.status = 'cancelled'
        ElMessage.success('已驳回')
        opLog.logStatusChange('驳回报名')
      } catch {
        ElMessage.error('驳回失败')
      }
    })
    .catch(() => {})
}
function handleBatchApprove() {
  ElMessageBox.confirm(`确定批量审核通过选中的 ${selectedIds.value.length} 项吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const targets = registrationList.value.filter(r => selectedIds.value.includes(r.id))
      let failed = 0
      for (const r of targets) {
        try {
          await registrationApi.update(r.id, { status: 'approved' })
          r.status = 'approved'
        } catch {
          failed++
        }
      }
      selectedIds.value = []
      if (failed > 0) {
        ElMessage.error(`批量审核失败，${failed} 项未成功`)
      } else {
        ElMessage.success('批量审核通过成功')
      }
    })
    .catch(() => {})
}
function handleBatchReject() {
  ElMessageBox.confirm(`确定批量驳回选中的 ${selectedIds.value.length} 项吗？`, '警告', { type: 'warning' })
    .then(async () => {
      const targets = registrationList.value.filter(r => selectedIds.value.includes(r.id))
      let failed = 0
      for (const r of targets) {
        try {
          await registrationApi.update(r.id, { status: 'cancelled' })
          r.status = 'cancelled'
        } catch {
          failed++
        }
      }
      selectedIds.value = []
      if (failed > 0) {
        ElMessage.error(`批量驳回失败，${failed} 项未成功`)
      } else {
        ElMessage.success('批量驳回成功')
      }
    })
    .catch(() => {})
}
function handleViewDetail(row: any) {
  detailRow.value = row
  detailDialogVisible.value = true
}
function handleExport() {
  const headers = ['报名号', '学生姓名', '学校', '年级', '参赛赛事', '报名状态', '报名时间', '联系电话', '备注']
  const rows = filteredList.value.map(r => [r.registerNo, r.studentName, r.school, r.grade, r.examName, statusLabel(r.status), r.registerTime, r.phone, r.remark || ''])
  const csv = [headers.join(','), ...rows.map(row => row.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `报名名单_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出报名数据')
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.registration-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}
</style>
