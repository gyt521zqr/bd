<template>
  <!-- 班级管理后台 -->
  <div class="class-manage-page">
    <div class="page-header">
      <h2>班级管理</h2>
      <el-button type="primary" @click="handleCreate">创建班级</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总班级数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.enabled }}</div>
        <div class="stat-label">启用中</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #86909C;">{{ stats.disabled }}</div>
        <div class="stat-label">已禁用</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #165DFF;">{{ stats.totalStudents }}</div>
        <div class="stat-label">总学员数</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索班级名称 / 授课教师"
        clearable
        style="width: 260px"
        @input="handleFilterChange"
        @clear="handleFilterChange"
      />
      <el-select
        v-model="statusFilter"
        placeholder="状态筛选"
        clearable
        style="width: 140px"
        @change="handleFilterChange"
      >
        <el-option label="已启用" value="enabled" />
        <el-option label="已禁用" value="disabled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 班级列表 -->
    <el-table :data="pagedList" stripe row-key="id" v-loading="loading" empty-text="暂无数据" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="班级名称" min-width="150" />
      <el-table-column prop="teacherName" label="授课教师" width="100" />
      <el-table-column prop="studentCount" label="学生数" width="80" align="center" />
      <el-table-column prop="maxStudents" label="最大容量" width="90" align="center" />
      <el-table-column label="容量" width="120">
        <template #default="{ row }">
          <el-progress
            :percentage="capacityPercentage(row as ClassRow)"
            :stroke-width="10"
            :status="capacityStatus(row as ClassRow)"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            active-value="enabled"
            inactive-value="disabled"
            @change="handleStatusChange(row as ClassRow)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row as ClassRow)">详情</el-button>
          <el-button size="small" text type="primary" @click="handleEdit(row as ClassRow)">编辑</el-button>
          <el-button size="small" text type="warning" @click="handleViewStudents(row as ClassRow)">查看学生</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row as ClassRow)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑班级' : '创建班级'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入班级名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherName">
          <el-input v-model="form.teacherName" placeholder="请输入授课教师姓名" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="最大容量" prop="maxStudents">
          <el-input-number v-model="form.maxStudents" :min="1" :max="500" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="enabled">启用</el-radio>
            <el-radio value="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看学生弹窗 -->
    <el-dialog
      v-model="studentsDialogVisible"
      :title="`班级学生列表${currentClass ? ' - ' + currentClass.name : ''}`"
      width="720px"
      destroy-on-close
    >
      <el-table :data="studentList" stripe v-loading="studentsLoading" max-height="420" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="学生姓名" min-width="100" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="parentPhone" label="家长电话" width="140" />
        <el-table-column prop="joinTime" label="加入时间" width="170" />
      </el-table>
      <template #footer>
        <el-button @click="studentsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 班级详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="班级详情" width="520px">
      <div v-if="viewRow" class="view-body" style="line-height: 2;">
        <p><strong>班级ID：</strong>{{ viewRow.id }}</p>
        <p><strong>班级名称：</strong>{{ viewRow.name }}</p>
        <p><strong>授课教师：</strong>{{ viewRow.teacherName }}</p>
        <p><strong>学生数：</strong>{{ viewRow.studentCount }}</p>
        <p><strong>最大容量：</strong>{{ viewRow.maxStudents }}</p>
        <p><strong>容量使用：</strong>{{ viewRow.studentCount }} / {{ viewRow.maxStudents }} ({{ capacityPercentage(viewRow) }}%)</p>
        <p><strong>状态：</strong>{{ viewRow.status === 'enabled' ? '已启用' : '已禁用' }}</p>
        <p><strong>创建时间：</strong>{{ viewRow.createTime }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * ClassManagePage - 班级管理后台
 */
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { classApi } from '@/api/admin'
import { getClassStudents, type StudentItem } from '@/api/class'
import { useOperationLog } from '@/composables/useOperationLog'
import Pagination from '@/components/common/Pagination.vue'

const opLog = useOperationLog('class')

// ==================== 类型定义 ====================
type ClassStatus = 'enabled' | 'disabled'

interface ClassRow {
  id: number
  name: string
  teacherName: string
  studentCount: number
  maxStudents: number
  createTime: string
  status: ClassStatus
}

interface StudentRow {
  id: number
  name: string
  phone: string
  parentPhone: string
  joinTime: string
}

interface ClassForm {
  name: string
  teacherName: string
  maxStudents: number
  status: ClassStatus
}

// ==================== 搜索与筛选 ====================
const keyword = ref('')
const statusFilter = ref<ClassStatus | ''>('')

// ==================== 分页 ====================
const pageNum = ref(1)
const pageSize = ref(20)

// ==================== 数据 ====================
const loading = ref(false)
const selectedIds = ref<number[]>([])
const classList = ref<ClassRow[]>([])

// ==================== 工具函数 ====================
function nowStr(): string {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

/** 将后端 TeachClass 实体映射为本地展示行；status 统一为 enabled/disabled 字符串 */
function mapClassItem(item: any): ClassRow {
  const st = item.status as unknown as string | number
  const status: ClassStatus = st === 'enabled' || st === 1 || st === '1' ? 'enabled' : 'disabled'
  return {
    id: item.id,
    name: item.name || item.className || '',
    teacherName: item.teacherName || '',
    studentCount: item.studentCount ?? item.currentStudent ?? 0,
    maxStudents: item.maxStudents ?? item.maxStudent ?? 40,
    createTime: item.createTime || nowStr(),
    status,
  }
}

/** 将接口返回的 StudentItem 映射为本地展示行 */
function mapStudentItem(item: StudentItem): StudentRow {
  return {
    id: item.id,
    name: item.name || '--',
    phone: item.phone || '--',
    parentPhone: item.parentPhone || '--',
    joinTime: item.joinTime || '--',
  }
}

function capacityPercentage(row: ClassRow): number {
  if (!row.maxStudents || row.maxStudents <= 0) return 0
  return Math.min(Math.round((row.studentCount / row.maxStudents) * 100), 100)
}

function capacityStatus(row: ClassRow): '' | 'success' | 'warning' | 'exception' {
  const p = capacityPercentage(row)
  if (p >= 90) return 'exception'
  if (p >= 70) return 'warning'
  return 'success'
}

// ==================== 加载数据 ====================
async function loadClasses() {
  loading.value = true
  try {
    const res = await classApi.list({ pageNum: 1, pageSize: 1000 })
    const list = res?.data?.list || []
    classList.value = list.map(mapClassItem)
  } catch {
    classList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadClasses()
})

// ==================== 过滤与分页 ====================
const filteredClassList = computed(() => {
  return classList.value.filter((item) => {
    const kw = keyword.value.trim()
    const matchKeyword = !kw || item.name.includes(kw) || item.teacherName.includes(kw)
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchStatus
  })
})

const total = computed(() => filteredClassList.value.length)

const stats = computed(() => {
  const total = classList.value.length
  const enabled = classList.value.filter(c => c.status === 'enabled').length
  const disabled = classList.value.filter(c => c.status === 'disabled').length
  const totalStudents = classList.value.reduce((sum, c) => sum + (c.studentCount || 0), 0)
  return { total, enabled, disabled, totalStudents }
})

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredClassList.value.slice(start, start + pageSize.value)
})

// ==================== 搜索 ====================
function handleSearch() {
  pageNum.value = 1
}

function handleFilterChange() {
  pageNum.value = 1
}

function handleReset() {
  keyword.value = ''
  statusFilter.value = ''
  pageNum.value = 1
}

function handleSelectionChange(rows: ClassRow[]) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 个班级吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await classApi.batchRemove(selectedIds.value)
        classList.value = classList.value.filter(c => !selectedIds.value.includes(c.id))
        selectedIds.value = []
        ElMessage.success('批量删除成功')
        opLog.logBatchDelete('批量删除班级')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

function handleExport() {
  const headers = ['ID', '班级名称', '授课教师', '学生数', '最大容量', '状态', '创建时间']
  const rows = filteredClassList.value.map(c => [c.id, c.name, c.teacherName, c.studentCount, c.maxStudents, c.status === 'enabled' ? '已启用' : '已禁用', c.createTime])
  const csv = [headers.join(','), ...rows.map(r => r.map(cell => `"${cell}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `班级数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出班级数据')
}

const viewDialogVisible = ref(false)
const viewRow = ref<ClassRow | null>(null)

function handleView(row: ClassRow) {
  viewRow.value = row
  viewDialogVisible.value = true
}

// ==================== 状态切换 ====================
async function handleStatusChange(row: ClassRow) {
  const newStatus = row.status
  const oldStatus: ClassStatus = newStatus === 'enabled' ? 'disabled' : 'enabled'
  try {
    await classApi.update(row.id, { status: newStatus === 'enabled' ? 1 : 0 })
    ElMessage.success(newStatus === 'enabled' ? `已启用「${row.name}」` : `已禁用「${row.name}」`)
  } catch {
    row.status = oldStatus
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}

// ==================== 删除 ====================
function handleDelete(row: ClassRow) {
  ElMessageBox.confirm(`确定删除班级「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
  })
    .then(async () => {
      try {
        await classApi.remove(row.id)
        const idx = classList.value.findIndex((c) => c.id === row.id)
        if (idx !== -1) classList.value.splice(idx, 1)
        // 当前页删空时回退到上一页，避免空白页
        const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
        if (pageNum.value > maxPage) pageNum.value = maxPage
        ElMessage.success('删除成功')
        opLog.logDelete('删除班级')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

// ==================== 弹窗表单 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editId = ref<number | null>(null)

function defaultForm(): ClassForm {
  return {
    name: '',
    teacherName: '',
    maxStudents: 40,
    status: 'enabled',
  }
}

const form = reactive<ClassForm>(defaultForm())

const formRules: FormRules = {
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入授课教师姓名', trigger: 'blur' }],
  maxStudents: [{ required: true, message: '请输入最大容量', trigger: 'blur' }],
}

function handleCreate() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

function handleEdit(row: ClassRow) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    name: row.name,
    teacherName: row.teacherName,
    maxStudents: row.maxStudents,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value && editId.value !== null) {
        await classApi.update(editId.value, {
          className: form.name,
          teacherName: form.teacherName,
          maxStudent: form.maxStudents,
          status: form.status === 'enabled' ? 1 : 0,
        })
        const idx = classList.value.findIndex((c) => c.id === editId.value)
        if (idx !== -1) {
          classList.value[idx] = {
            ...classList.value[idx],
            name: form.name,
            teacherName: form.teacherName,
            maxStudents: form.maxStudents,
            status: form.status,
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const res = await classApi.create({
          className: form.name,
          teacherName: form.teacherName,
          maxStudent: form.maxStudents,
          status: form.status === 'enabled' ? 1 : 0,
        })
        if (res?.data?.id) {
          classList.value.unshift(mapClassItem(res.data))
        }
        ElMessage.success('创建成功')
        opLog.logCreate('新增班级')
        pageNum.value = 1
      }
      dialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// ==================== 查看学生 ====================
const studentsDialogVisible = ref(false)
const currentClass = ref<ClassRow | null>(null)
const studentList = ref<StudentRow[]>([])
const studentsLoading = ref(false)

async function handleViewStudents(row: ClassRow) {
  currentClass.value = row
  studentList.value = []
  studentsDialogVisible.value = true
  studentsLoading.value = true
  try {
    const res = await getClassStudents(row.id, { pageNum: 1, pageSize: 1000 })
    if (res?.code === 200 && res.data?.list?.length) {
      studentList.value = res.data.list.map(mapStudentItem)
    }
  } catch {
    studentList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  } finally {
    studentsLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.class-manage-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    font-size: 20px;
    color: #1D2129;
  }
}

.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.text-muted {
  color: #86909C;
}
</style>
