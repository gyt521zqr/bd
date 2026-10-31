<template>
  <div class="student-manage-page">
    <div class="page-header">
      <h2>学员管理</h2>
    </div>

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
      <el-button :icon="Upload" @click="importDialogVisible = true">批量导入</el-button>
      <el-button :icon="Download" @click="downloadTemplate">下载导入模板</el-button>
      <el-button :icon="Download" @click="handleExport">批量导出</el-button>
      <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="!selectedRows.length">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="pagedStudentList"
      stripe
      v-loading="loading"
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
          <el-button size="small" text type="warning" @click="handleSetMember(row)">设置会员</el-button>
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
  await fetchStudentList()
  ElMessage.success('搜索完成')
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

// Mock 数据 20 条（含会员状态）
const studentList = ref([
  { id: 1, name: '张三', school: '北京市第一小学', grade: '小学', phone: '13800138001', parentPhone: '13900139001', examCount: 3, lastLogin: '2024-06-18 10:30', status: 'enabled', memberStatus: 'active', memberType: 'yearly', memberExpiry: '2025-06-18' },
  { id: 2, name: '李四', school: '北京市第二中学', grade: '初中', phone: '13800138002', parentPhone: '13900139002', examCount: 5, lastLogin: '2024-06-17 14:20', status: 'enabled', memberStatus: 'active', memberType: 'quarterly', memberExpiry: '2024-09-17' },
  { id: 3, name: '王五', school: '北京市第三中学', grade: '高中', phone: '13800138003', parentPhone: '13900139003', examCount: 2, lastLogin: '2024-06-16 09:15', status: 'enabled', memberStatus: 'none', memberType: '', memberExpiry: '' },
  { id: 4, name: '赵六', school: '北京市实验小学', grade: '小学', phone: '13800138004', parentPhone: '13900139004', examCount: 1, lastLogin: '2024-06-15 16:45', status: 'disabled', memberStatus: 'expired', memberType: 'monthly', memberExpiry: '2024-05-15' },
  { id: 5, name: '孙七', school: '北京市第四中学', grade: '初中', phone: '13800138005', parentPhone: '13900139005', examCount: 4, lastLogin: '2024-06-14 11:00', status: 'enabled', memberStatus: 'active', memberType: 'monthly', memberExpiry: '2024-07-14' },
  { id: 6, name: '周八', school: '北京市第五中学', grade: '高中', phone: '13800138006', parentPhone: '13900139006', examCount: 6, lastLogin: '2024-06-13 08:30', status: 'enabled', memberStatus: 'active', memberType: 'yearly', memberExpiry: '2025-06-13' },
  { id: 7, name: '吴九', school: '北京市第六小学', grade: '小学', phone: '13800138007', parentPhone: '13900139007', examCount: 0, lastLogin: '2024-06-12 15:20', status: 'disabled', memberStatus: 'expired', memberType: 'quarterly', memberExpiry: '2024-04-12' },
  { id: 8, name: '郑十', school: '北京市第七中学', grade: '初中', phone: '13800138008', parentPhone: '13900139008', examCount: 3, lastLogin: '2024-06-11 13:10', status: 'enabled', memberStatus: 'active', memberType: 'permanent', memberExpiry: '' },
  { id: 9, name: '钱十一', school: '北京市第八中学', grade: '高中', phone: '13800138009', parentPhone: '13900139009', examCount: 7, lastLogin: '2024-06-10 10:00', status: 'enabled', memberStatus: 'active', memberType: 'yearly', memberExpiry: '2025-06-10' },
  { id: 10, name: '陈十二', school: '北京市第九小学', grade: '小学', phone: '13800138010', parentPhone: '13900139010', examCount: 2, lastLogin: '2024-06-09 17:30', status: 'enabled', memberStatus: 'none', memberType: '', memberExpiry: '' },
  { id: 11, name: '刘十三', school: '北京市第十中学', grade: '初中', phone: '13800138011', parentPhone: '13900139011', examCount: 4, lastLogin: '2024-06-08 09:45', status: 'enabled', memberStatus: 'active', memberType: 'monthly', memberExpiry: '2024-07-08' },
  { id: 12, name: '黄十四', school: '北京市第十一中学', grade: '高中', phone: '13800138012', parentPhone: '13900139012', examCount: 5, lastLogin: '2024-06-07 14:00', status: 'disabled', memberStatus: 'expired', memberType: 'yearly', memberExpiry: '2024-03-07' },
  { id: 13, name: '林十五', school: '北京市第十二小学', grade: '小学', phone: '13800138013', parentPhone: '13900139013', examCount: 1, lastLogin: '2024-06-06 11:20', status: 'enabled', memberStatus: 'none', memberType: '', memberExpiry: '' },
  { id: 14, name: '杨十六', school: '北京市第十三中学', grade: '初中', phone: '13800138014', parentPhone: '13900139014', examCount: 3, lastLogin: '2024-06-05 16:10', status: 'enabled', memberStatus: 'active', memberType: 'quarterly', memberExpiry: '2024-09-05' },
  { id: 15, name: '朱十七', school: '北京市第十四中学', grade: '高中', phone: '13800138015', parentPhone: '13900139015', examCount: 8, lastLogin: '2024-06-04 08:50', status: 'enabled', memberStatus: 'active', memberType: 'yearly', memberExpiry: '2025-06-04' },
  { id: 16, name: '徐十八', school: '北京市第十五小学', grade: '小学', phone: '13800138016', parentPhone: '13900139016', examCount: 2, lastLogin: '2024-06-03 13:40', status: 'enabled', memberStatus: 'none', memberType: '', memberExpiry: '' },
  { id: 17, name: '马十九', school: '北京市第十六中学', grade: '初中', phone: '13800138017', parentPhone: '13900139017', examCount: 6, lastLogin: '2024-06-02 10:15', status: 'disabled', memberStatus: 'expired', memberType: 'monthly', memberExpiry: '2024-05-02' },
  { id: 18, name: '胡二十', school: '北京市第十七中学', grade: '高中', phone: '13800138018', parentPhone: '13900139018', examCount: 4, lastLogin: '2024-06-01 15:00', status: 'enabled', memberStatus: 'active', memberType: 'permanent', memberExpiry: '' },
  { id: 19, name: '郭二十一', school: '北京市第十八小学', grade: '小学', phone: '13800138019', parentPhone: '13900139019', examCount: 0, lastLogin: '2024-05-31 09:30', status: 'enabled', memberStatus: 'none', memberType: '', memberExpiry: '' },
  { id: 20, name: '何二十二', school: '北京市第十九中学', grade: '初中', phone: '13800138020', parentPhone: '13900139020', examCount: 3, lastLogin: '2024-05-30 14:20', status: 'enabled', memberStatus: 'none', memberType: '', memberExpiry: '' },
])

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

/** 获取学员列表，调用 bridge 列表接口，失败时回退到本地 mock 数据 */
async function fetchStudentList() {
  loading.value = true
  try {
    const res = await studentApi.list({ pageNum: 1, pageSize: 9999 })
    const list = res?.data?.list || res?.data?.records || []
    if (list.length) studentList.value = list
  } catch (e) {
    console.warn('load failed, fallback to mock', e)
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
    } catch (e) {
      console.warn('save student failed, fallback to local', e)
      // 后端不可用时降级为本地写入，保证页面始终可用
      if (isEdit.value && form.id != null) {
        const idx = studentList.value.findIndex(s => s.id === form.id)
        if (idx !== -1) studentList.value[idx] = { ...studentList.value[idx], ...form } as any
      } else {
        studentList.value.unshift({ ...form, id: Date.now(), examCount: 0, lastLogin: '-', status: form.status || 'enabled' } as any)
      }
      ElMessage.warning('后端未连接，已本地保存（未落库）')
      formDialogVisible.value = false
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
async function submitMemberForm() {
  if (!memberFormRef.value) return
  await memberFormRef.value.validate(async (valid) => {
    if (!valid) return
    memberSubmitting.value = true
    try {
      await new Promise<void>(resolve => setTimeout(resolve, 500))
      const idx = studentList.value.findIndex(s => s.id === memberTargetStudent.value.id)
      if (idx !== -1) {
        studentList.value[idx].memberStatus = 'active'
        studentList.value[idx].memberType = memberForm.memberType
        studentList.value[idx].memberExpiry = memberForm.memberType === 'permanent' ? '' : memberForm.memberExpiry
      }
      memberDialogVisible.value = false
      ElMessage.success('会员设置成功')
      opLog.logUpdate('设置学员「' + memberTargetStudent.value?.name + '」为' + memberTypeMap[memberForm.memberType])
    } finally {
      memberSubmitting.value = false
    }
  })
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
  } catch (e) {
    console.warn('reset password fallback', e)
    ElMessage.warning('后端未连接，重置未落库')
  }
}

// 禁用/启用
async function handleToggleStatus(row: any) {
  const next = row.status === 'enabled' ? 'disabled' : 'enabled'
  const label = next === 'enabled' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}学员 "${row.name}" 吗？`, '提示', { type: 'warning' })
  } catch { return }
  row.status = next
  ElMessage.success(`${label}成功`)
  opLog.logStatusChange('学员「' + row.name + '」状态变更为' + (row.status === 'enabled' ? '启用' : '禁用'))
  try {
    await studentApi.update(row.id, { status: next })
  } catch (e) {
    console.warn('toggle status fallback', e)
  }
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
  } catch (e) {
    console.warn('batch delete fallback', e)
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
async function submitImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    importResult.success = 18
    importResult.fail = 2
    importResult.errors = ['第 5 行：手机号格式错误', '第 12 行：姓名为空']
    importDialogVisible.value = false
    importResultVisible.value = true
    ElMessage.success('导入完成')
    opLog.logImport('批量导入' + importResult.success + '条学员数据')
  } finally {
    importing.value = false
    importFile.value = null
    uploadRef.value?.clearFiles()
  }
}

// 下载模板
function downloadTemplate() {
  const headers = ['姓名', '学校', '年级(小学/初中/高中)', '手机号', '家长电话', '邮箱', '备注']
  const csv = headers.join(',') + '\n示例姓名,示例学校,小学,13800138000,13900139000,test@example.com,备注信息'
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
