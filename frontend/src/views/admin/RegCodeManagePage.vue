<template>
  <!-- 注册码管理 -->
  <div class="reg-code-manage-page">
    <div class="page-header">
      <h2>注册码管理</h2>
      <div class="header-actions">
        <el-button type="primary" title="暂未接入后端服务" @click="openGenerateDialog">批量生成</el-button>
        <el-button @click="handleBatchExport">批量导出</el-button>
        <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </div>

    <el-alert
      title="功能提示"
      description="批量生成注册码暂未接入后端服务，操作不会保存。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <p class="page-desc">注册码用于线上竞赛的身份校验，防止非报名人员参赛。</p>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总注册码数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.used }}</div>
        <div class="stat-label">已使用</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #165DFF;">{{ stats.unused }}</div>
        <div class="stat-label">未使用</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #86909C;">{{ stats.expired }}</div>
        <div class="stat-label">已过期</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.code" placeholder="搜索注册码" clearable style="width: 200px" />
      <el-select v-model="searchForm.examId" placeholder="关联赛事" clearable style="width: 180px">
        <el-option v-for="exam in examOptions" :key="exam.value" :label="exam.label" :value="exam.value" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="未使用" value="unused" />
        <el-option label="已使用" value="used" />
        <el-option label="已过期" value="expired" />
        <el-option label="已禁用" value="disabled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="pagedList" stripe @selection-change="handleSelectionChange" v-loading="loading" empty-text="暂无数据">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="code" label="注册码" width="140" />
      <el-table-column prop="examName" label="关联赛事" min-width="160" />
      <el-table-column prop="generateTime" label="生成时间" width="170" />
      <el-table-column prop="expireTime" label="有效期" width="170" />
      <el-table-column prop="userName" label="使用人" width="120">
        <template #default="{ row }">
          <span>{{ row.userName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="useTime" label="使用时间" width="170">
        <template #default="{ row }">
          <span>{{ row.useTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row)">查看详情</el-button>
          <el-button size="small" text :type="row.status === 'disabled' ? 'success' : 'warning'" @click="handleToggle(row)">
            {{ row.status === 'disabled' ? '启用' : '禁用' }}
          </el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="filteredList.length" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 批量生成弹窗 -->
    <el-dialog v-model="generateDialogVisible" title="批量生成注册码" width="500px" destroy-on-close>
      <el-form ref="generateFormRef" :model="generateForm" :rules="generateRules" label-width="100px">
        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="generateForm.count" :min="1" :max="500" style="width: 100%" />
        </el-form-item>
        <el-form-item label="关联赛事" prop="examId">
          <el-select v-model="generateForm.examId" placeholder="选择关联赛事" style="width: 100%">
            <el-option v-for="exam in examOptions" :key="exam.value" :label="exam.label" :value="exam.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期至" prop="expireTime">
          <el-date-picker v-model="generateForm.expireTime" type="datetime" placeholder="选择有效期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="submitGenerate">确定生成</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="注册码详情" width="500px">
      <div v-if="viewRow" class="view-body">
        <p><strong>注册码：</strong><span class="code-highlight">{{ viewRow.code }}</span></p>
        <p><strong>关联赛事：</strong>{{ viewRow.examName }}</p>
        <p><strong>生成时间：</strong>{{ viewRow.generateTime }}</p>
        <p><strong>有效期至：</strong>{{ viewRow.expireTime }}</p>
        <p><strong>使用人：</strong>{{ viewRow.userName || '未使用' }}</p>
        <p><strong>使用时间：</strong>{{ viewRow.useTime || '-' }}</p>
        <p><strong>状态：</strong><el-tag :type="statusTagType(viewRow.status)" size="small">{{ statusMap[viewRow.status] }}</el-tag></p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * RegCodeManagePage - 注册码管理
 */
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('regcode')

const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const selectedIds = ref<number[]>([])

const statusMap: Record<string, string> = {
  unused: '未使用',
  used: '已使用',
  expired: '已过期',
  disabled: '已禁用',
}

const examOptions = [
  { value: 1, label: 'Python期中测试' },
  { value: 2, label: 'C++算法模拟考' },
  { value: 3, label: '期末综合考试' },
  { value: 4, label: 'Scratch创意赛' },
  { value: 5, label: '信息学奥赛初赛' },
]

function statusTagType(status: string) {
  const map: Record<string, any> = {
    unused: 'primary',
    used: 'success',
    expired: 'info',
    disabled: 'danger',
  }
  return map[status] || ''
}

const codeList = ref<any[]>([])

function saveCodeToLocal() {
  setLocalData(STORAGE_KEYS.REG_CODE_LIST, codeList.value)
}

const stats = computed(() => {
  const total = codeList.value.length
  const used = codeList.value.filter(c => c.status === 'used').length
  const unused = codeList.value.filter(c => c.status === 'unused').length
  const expired = codeList.value.filter(c => c.status === 'expired').length
  return { total, used, unused, expired }
})

const searchForm = reactive({
  code: '',
  examId: null as number | null,
  status: '',
})

function handleSearch() { pageNum.value = 1 }
function resetSearch() {
  searchForm.code = ''
  searchForm.examId = null
  searchForm.status = ''
  pageNum.value = 1
}

const filteredList = computed(() => {
  let list = codeList.value
  if (searchForm.code) {
    list = list.filter(c => c.code.includes(searchForm.code.toUpperCase()))
  }
  if (searchForm.examId) {
    list = list.filter(c => c.examId === searchForm.examId)
  }
  if (searchForm.status) {
    list = list.filter(c => c.status === searchForm.status)
  }
  return list
})

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}
function handleBatchDelete() {
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个注册码吗？`, '警告', { type: 'warning' })
    .then(() => {
      codeList.value = codeList.value.filter(c => !selectedIds.value.includes(c.id))
      saveCodeToLocal()
      ElMessage.success('批量删除成功')
      selectedIds.value = []
    })
    .catch(() => {})
}
function handleBatchExport() {
  const headers = ['ID', '注册码', '关联赛事', '生成时间', '有效期', '使用人', '使用时间', '状态']
  const rows = filteredList.value.map(c => [c.id, c.code, c.examName, c.generateTime, c.expireTime, c.userName || '', c.useTime || '', statusMap[c.status] || c.status])
  const csv = [headers.join(','), ...rows.map(r => r.map(cell => `"${cell}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `注册码数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出注册码数据')
}
function handleView(row: any) {
  viewRow.value = row
  viewDialogVisible.value = true
}
function handleToggle(row: any) {
  row.status = row.status === 'disabled' ? 'unused' : 'disabled'
  saveCodeToLocal()
  ElMessage.success(`已${row.status === 'disabled' ? '禁用' : '启用'}注册码`)
}
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定删除注册码 "${row.code}" 吗？`, '警告', { type: 'warning' })
    .then(() => {
      codeList.value = codeList.value.filter(c => c.id !== row.id)
      saveCodeToLocal()
      ElMessage.success('删除成功')
      opLog.logDelete('删除注册码')
    })
    .catch(() => {})
}

// 批量生成
const generateDialogVisible = ref(false)
const generating = ref(false)
const generateFormRef = ref<FormInstance>()
const generateForm = reactive({
  count: 10,
  examId: null as number | null,
  expireTime: '',
})
const generateRules: FormRules = {
  count: [{ required: true, message: '请输入生成数量', trigger: 'change' }],
  examId: [{ required: true, message: '请选择关联赛事', trigger: 'change' }],
  expireTime: [{ required: true, message: '请选择有效期', trigger: 'change' }],
}

function openGenerateDialog() {
  generateForm.count = 10
  generateForm.examId = null
  generateForm.expireTime = ''
  generateDialogVisible.value = true
}

function submitGenerate() {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}

// 查看详情
const viewDialogVisible = ref(false)
const viewRow = ref<any>(null)

onMounted(() => {
  const localData = getLocalData<any[] | null>(STORAGE_KEYS.REG_CODE_LIST, null)
  if (localData && localData.length > 0) {
    codeList.value = localData
  }
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.reg-code-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;
  h2 { font-size: 20px; color: #1D2129; }
  .header-actions { display: flex; gap: 8px; }
}
.page-desc { color: #666; font-size: 13px; margin-bottom: 16px; }

.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}

.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.view-body { line-height: 1.8; color: $text-primary;
  p { margin-bottom: 8px; }
  .code-highlight { font-family: monospace; font-size: 16px; color: $primary; font-weight: 600; letter-spacing: 2px; background: rgba($primary, 0.08); padding: 4px 12px; border-radius: 4px; }
}
</style>
