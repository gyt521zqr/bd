<template>
  <div class="student-manage-page">
    <div class="page-header">
      <h2>学员管理</h2>
    </div>

    <el-alert
      title="功能提示"
      description="设置会员、批量导入暂未接入后端服务，操作不会保存。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--total">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ totalStudentCount }}</div>
          <div class="stat-card__label">总学员数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--active">
          <el-icon :size="28"><Star /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ activeMemberCount }}</div>
          <div class="stat-card__label">当前会员数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--expired">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ expiredMemberCount }}</div>
          <div class="stat-card__label">历史会员数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--new">
          <el-icon :size="28"><TrendCharts /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ newMemberThisMonth }}</div>
          <div class="stat-card__label">本月新增会员</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="姓名" clearable style="width: 160px" />
      <el-input v-model="searchForm.school" placeholder="学校" clearable style="width: 180px" />
      <el-select v-model="searchForm.grade" placeholder="年级" clearable style="width: 140px">
        <el-option label="小学" value="小学" />
        <el-option label="初中" value="初中" />
        <el-option label="高中" value="高中" />
      </el-select>
      <el-input v-model="searchForm.phone" placeholder="手机号" clearable style="width: 160px" />
      <el-select v-model="searchForm.memberStatus" placeholder="会员状态" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option label="当前会员" value="active" />
        <el-option label="历史会员" value="expired" />
        <el-option label="非会员" value="none" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加学员</el-button>
      <el-button :icon="Upload" title="暂未接入后端服务" @click="importDialogVisible = true">批量导入</el-button>
      <el-button :icon="Download" @click="downloadTemplate">下载导入模板</el-button>
      <el-button :icon="Download" @click="handleExport">批量导出</el-button>
      <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="!selectedRows.length">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="pagedStudentList"
      stripe
      v-loading="loading"
      empty-text="暂无数据"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="school" label="学校" min-width="160" />
      <el-table-column prop="grade" label="年级" width="80" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="parentPhone" label="家长电话" width="120" />
      <el-table-column prop="examCount" label="参赛次数" width="90" />
      <el-table-column prop="lastLogin" label="最近登录" width="160" />
      <el-table-column prop="memberStatus" label="会员状态" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.memberStatus === 'active'" type="warning" effect="dark" size="small">当前会员</el-tag>
          <el-tag v-else-if="row.memberStatus === 'expired'" type="info" size="small">历史会员</el-tag>
          <el-tag v-else type="info" effect="plain" size="small">非会员</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'danger'" size="small">
            {{ row.status === 'enabled' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" text type="primary" @click="handleViewDetail(row)">查看详情</el-button>
          <el-button size="small" text type="warning" @click="handleResetPassword(row)">重置密码</el-button>
          <el-button size="small" text :type="row.status === 'enabled' ? 'danger' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 'enabled' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" text type="warning" title="暂未接入后端服务" @click="handleSetMember(row)">设置会员</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑学员' : '添加学员'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学校" prop="school">
          <el-input v-model="form.school" placeholder="请输入学校" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select v-model="form.grade" placeholder="选择年级" style="width: 100%">
            <el-option label="小学" value="小学" />
            <el-option label="初中" value="初中" />
            <el-option label="高中" value="高中" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="家长电话" prop="parentPhone">
          <el-input v-model="form.parentPhone" placeholder="请输入家长电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-divider />
        <el-form-item label="会员状态" prop="memberStatus">
          <el-select v-model="form.memberStatus" placeholder="选择会员状态" style="width: 100%">
            <el-option label="非会员" value="none" />
            <el-option label="当前会员" value="active" />
            <el-option label="历史会员" value="expired" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.memberStatus === 'active'" label="会员类型" prop="memberType">
          <el-select v-model="form.memberType" placeholder="选择会员类型" style="width: 100%">
            <el-option label="月度会员" value="monthly" />
            <el-option label="季度会员" value="quarterly" />
            <el-option label="年度会员" value="yearly" />
            <el-option label="永久会员" value="permanent" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.memberStatus === 'active'" label="会员到期日" prop="memberExpiry">
          <el-date-picker v-model="form.memberExpiry" type="date" placeholder="选择到期日" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设置会员弹窗 -->
    <el-dialog v-model="memberDialogVisible" title="设置会员" width="480px" destroy-on-close>
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberFormRules" label-width="90px">
        <el-form-item label="学员姓名">
          <el-input :model-value="memberTargetStudent?.name" disabled />
        </el-form-item>
        <el-form-item label="会员类型" prop="memberType">
          <el-select v-model="memberForm.memberType" placeholder="选择会员类型" style="width: 100%">
            <el-option label="月度会员" value="monthly" />
            <el-option label="季度会员" value="quarterly" />
            <el-option label="年度会员" value="yearly" />
            <el-option label="永久会员" value="permanent" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="memberForm.memberType !== 'permanent'" label="到期时间" prop="memberExpiry">
          <el-date-picker v-model="memberForm.memberExpiry" type="date" placeholder="选择到期日" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="memberSubmitting" @click="submitMemberForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="批量导入学员" width="520px" destroy-on-close>
      <el-upload
        ref="uploadRef"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 格式</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="submitImport">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 导入结果弹窗 -->
    <el-dialog v-model="importResultVisible" title="导入结果" width="480px">
      <div class="import-result">
        <p><strong>导入成功：</strong>{{ importResult.success }} 条</p>
        <p><strong>导入失败：</strong>{{ importResult.fail }} 条</p>
        <div v-if="importResult.errors.length">
          <p style="margin-top: 12px;"><strong>失败原因：</strong></p>
          <ul class="error-list">
            <li v-for="(err, idx) in importResult.errors" :key="idx">{{ err }}</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="importResultVisible = false">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="学员详情" width="480px">
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentRow.school }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ currentRow.grade }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentRow.phone }}</el-descriptions-item>
        <el-descriptions-item label="家长电话">{{ currentRow.parentPhone }}</el-descriptions-item>
        <el-descriptions-item label="参赛次数">{{ currentRow.examCount }}</el-descriptions-item>
        <el-descriptions-item label="最近登录">{{ currentRow.lastLogin }}</el-descriptions-item>
        <el-descriptions-item label="会员状态">
          <el-tag v-if="currentRow.memberStatus === 'active'" type="warning" effect="dark" size="small">当前会员</el-tag>
          <el-tag v-else-if="currentRow.memberStatus === 'expired'" type="info" size="small">历史会员</el-tag>
          <el-tag v-else type="info" effect="plain" size="small">非会员</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow.memberType" label="会员类型">{{ memberTypeMap[currentRow.memberType] }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.memberExpiry" label="会员到期日">{{ currentRow.memberExpiry }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentRow.status === 'enabled' ? '启用' : '禁用' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * StudentManagePage - 学员管理页面
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Upload, Download, Delete, User, Star, Clock, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadInstance } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { studentApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('student')

// 会员类型映射
const memberTypeMap: Record<string, string> = {
  monthly: '月度会员',
  quarterly: '季度会员',
  yearly: '年度会员',
  permanent: '永久会员',
}

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const selectedRows = ref<any[]>([])

const searchForm = reactive({
  name: '',
  school: '',
  grade: '',
  phone: '',
  memberStatus: '',
})

async function handleSearch() {
  pageNum.value = 1
  if (await fetchStudentList()) ElMessage.success('搜索完成')
}
function resetSearch() {
  searchForm.name = ''
  searchForm.school = ''
  searchForm.grade = ''
  searchForm.phone = ''
  searchForm.memberStatus = ''
  pageNum.value = 1
  fetchStudentList()
}

function handleSelectionChange(val: any[]) {
  selectedRows.value = val
}

const studentList = ref<any[]>([])

// 按搜索条件筛选后的列表（name/school/grade/phone/memberStatus）
const filteredStudentList = computed(() => {
  return studentList.value.filter(s => {
    if (searchForm.name && !s.name.toLowerCase().includes(searchForm.name.toLowerCase())) return false
    if (searchForm.school && !s.school.toLowerCase().includes(searchForm.school.toLowerCase())) return false
    if (searchForm.grade && s.grade !== searchForm.grade) return false
    if (searchForm.phone && !s.phone.includes(searchForm.phone)) return false
    if (searchForm.memberStatus && s.memberStatus !== searchForm.memberStatus) return false
    return true
  })
})

// 当前页展示数据（分页切片）
const pagedStudentList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredStudentList.value.slice(start, start + pageSize.value)
})

// 总条数：跟随筛选结果动态变化
const total = computed(() => filteredStudentList.value.length)

/** 获取学员列表（仅来自后端；失败清空并提示，不回填本地数据） */
async function fetchStudentList(): Promise<boolean> {
  loading.value = true
  try {
    const res = await studentApi.list({ pageNum: 1, pageSize: 9999 })
    studentList.value = res?.data?.list || res?.data?.records || []
    return true
  } catch {
    studentList.value = []
    ElMessage.error('数据加载失败，请检查网络或稍后重试')
    return false
  } finally {
    loading.value = false
  }
}

// 统计数据
const totalStudentCount = computed(() => studentList.value.length)
const activeMemberCount = computed(() => studentList.value.filter(s => s.memberStatus === 'active').length)
const expiredMemberCount = computed(() => studentList.value.filter(s => s.memberStatus === 'expired').length)
const newMemberThisMonth = computed(() => {
  const now = new Date()
  const curYear = now.getFullYear()
  const curMonth = now.getMonth() + 1
  const curYM = `${curYear}-${String(curMonth).padStart(2, '0')}`
  return studentList.value.filter(s => {
    if (s.memberStatus !== 'active') return false
    if (s.memberExpiry) {
      // memberExpiry 格式 'YYYY-MM-DD'，到期年月在当月或之后视为本月新增会员
      const [y, m] = s.memberExpiry.split('-').map(Number)
      return y > curYear || (y === curYear && m >= curMonth)
    }
    // 无 memberExpiry 数据时，近似判断 lastLogin 是否以当前年月开头
    return s.lastLogin && String(s.lastLogin).startsWith(curYM)
  }).length
})

// 添加/编辑
const formDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: null as number | null,
  name: '',
  school: '',
  grade: '',
  phone: '',
  parentPhone: '',
  email: '',
  remark: '',
  status: 'enabled' as string,
  memberStatus: 'none' as string,
  memberType: '' as string,
  memberExpiry: '' as string,
})
const formRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  school: [{ required: true, message: '请输入学校', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', school: '', grade: '', phone: '', parentPhone: '', email: '', remark: '', status: 'enabled', memberStatus: 'none', memberType: '', memberExpiry: '' })
  formDialogVisible.value = true
}
function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(form, row)
  formDialogVisible.value = true
}
async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 仅将可落库的身份字段（姓名/手机/邮箱/状态）写入 sys_user；会员档案字段（学校/年级/
      // 家长电话/会员状态等）后端暂无独立表，保留本地展示，属已知缺口。
      const payload: any = {
        name: form.name,
        phone: form.phone,
        email: form.email,
        status: form.status || 'enabled',
      }
      if (isEdit.value && form.id != null) {
        await studentApi.update(form.id as number, payload)
        const idx = studentList.value.findIndex(s => s.id === form.id)
        if (idx !== -1) studentList.value[idx] = { ...studentList.value[idx], ...form } as any
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑学员「' + form.name + '」')
      } else {
        const res: any = await studentApi.create(payload)
        const created = res?.data
        const newId = created?.id ?? Date.now()
        studentList.value.unshift({ ...form, id: newId, examCount: 0, lastLogin: '-', status: form.status || 'enabled' } as any)
        ElMessage.success('添加成功')
        opLog.logCreate('新增学员「' + form.name + '」')
      }
      formDialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 设置会员弹窗
const memberDialogVisible = ref(false)
const memberSubmitting = ref(false)
const memberFormRef = ref<FormInstance>()
const memberTargetStudent = ref<any>(null)
const memberForm = reactive({
  memberType: '' as string,
  memberExpiry: '' as string,
})
const memberFormRules: FormRules = {
  memberType: [{ required: true, message: '请选择会员类型', trigger: 'change' }],
  memberExpiry: [{ required: true, message: '请选择到期时间', trigger: 'change' }],
}

function handleSetMember(row: any) {
  memberTargetStudent.value = row
  memberForm.memberType = row.memberType || ''
  memberForm.memberExpiry = row.memberExpiry || ''
  memberDialogVisible.value = true
}
function submitMemberForm() {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}

// 查看详情
const detailDialogVisible = ref(false)
const currentRow = ref<any>(null)
function handleViewDetail(row: any) {
  currentRow.value = row
  detailDialogVisible.value = true
}

// 重置密码
async function handleResetPassword(row: any) {
  try {
    await ElMessageBox.confirm(`确定将学员 "${row.name}" 的密码重置为 123456 吗？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await studentApi.update(row.id, { password: '123456' })
    ElMessage.success('密码已重置为 123456')
    opLog.logUpdate('重置学员「' + row.name + '」的密码')
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}

// 禁用/启用
async function handleToggleStatus(row: any) {
  const next = row.status === 'enabled' ? 'disabled' : 'enabled'
  const label = next === 'enabled' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}学员 "${row.name}" 吗？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await studentApi.update(row.id, { status: next })
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
    return
  }
  row.status = next
  ElMessage.success(`${label}成功`)
  opLog.logStatusChange('学员「' + row.name + '」状态变更为' + (row.status === 'enabled' ? '启用' : '禁用'))
}

// 批量删除
async function handleBatchDelete() {
  const names = selectedRows.value.map(r => r.name).join('、')
  try {
    await ElMessageBox.confirm(`确定删除以下 ${selectedRows.value.length} 名学员吗？\n${names}`, '警告', { type: 'warning' })
  } catch { return }
  const ids = selectedRows.value.map(r => r.id)
  try {
    await studentApi.batchRemove(ids)
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
    return
  }
  studentList.value = studentList.value.filter(s => !ids.includes(s.id))
  ElMessage.success('删除成功')
  opLog.logBatchDelete('批量删除' + selectedRows.value.length + '名学员')
}

// 批量导入
const importDialogVisible = ref(false)
const importing = ref(false)
const uploadRef = ref<UploadInstance>()
const importFile = ref<File | null>(null)
const importResultVisible = ref(false)
const importResult = reactive({ success: 0, fail: 0, errors: [] as string[] })

function handleFileChange(file: any) {
  importFile.value = file.raw
}
function submitImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}

// 下载模板
function downloadTemplate() {
  const headers = ['姓名', '学校', '年级(小学/初中/高中)', '手机号', '家长电话', '邮箱', '备注']
  const csv = headers.join(',')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '学员导入模板.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('模板下载成功')
}

// 批量导出
function handleExport() {
  const data = selectedRows.value.length ? selectedRows.value : studentList.value
  const headers = ['ID', '姓名', '学校', '年级', '手机号', '家长电话', '参赛次数', '最近登录', '会员状态', '状态']
  const rows = data.map(s => [s.id, s.name, s.school, s.grade, s.phone, s.parentPhone, s.examCount, s.lastLogin, s.memberStatus === 'active' ? '当前会员' : s.memberStatus === 'expired' ? '历史会员' : '非会员', s.status === 'enabled' ? '启用' : '禁用'])
  const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `学员列表_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出学员数据')
}

onMounted(() => {
  fetchStudentList()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.student-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: $bg-white;
  border-radius: 8px;
  border: 1px solid #F7F8FA;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08); }
  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    border-radius: 12px;
    flex-shrink: 0;
    &--total { background: rgba(22, 93, 255, 0.1); color: $primary; }
    &--active { background: rgba(230, 162, 60, 0.1); color: #FF9500; }
    &--expired { background: rgba(144, 147, 153, 0.1); color: #86909C; }
    &--new { background: rgba(103, 194, 58, 0.1); color: #36D399; }
  }
  &__content { flex: 1; min-width: 0; }
  &__value { font-size: 24px; font-weight: 600; color: #1D2129; line-height: 1.2; }
  &__label { font-size: 13px; color: #86909C; margin-top: 4px; }
}

.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.action-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.import-result { p { margin: 8px 0; } }
.error-list { color: #F53F3F; margin: 8px 0; padding-left: 20px; li { margin: 4px 0; } }
</style>
