<template>
  <!-- 赛事管理后台 -->
  <div class="exam-manage-page">
    <div class="page-header">
      <h2>赛事管理</h2>
      <el-button type="primary" @click="handleCreate">创建赛事</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总赛事数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.active }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #FF9500;">{{ stats.pending }}</div>
        <div class="stat-label">未开始</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #86909C;">{{ stats.ended }}</div>
        <div class="stat-label">已结束</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索赛事名称" clearable style="width: 240px" />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px">
        <el-option label="进行中" value="active" />
        <el-option label="未开始" value="pending" />
        <el-option label="已结束" value="ended" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除</el-button>
      <el-button v-if="selectedIds.length > 0" type="success" @click="handleBatchPublish">批量发布</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <el-table :data="pagedList" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="赛事名称" min-width="200" />
      <el-table-column prop="duration" label="时长(分)" width="90" />
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column prop="questionCount" label="题目数" width="80" />
      <el-table-column prop="participantCount" label="参赛人数" width="90" />
      <el-table-column prop="registerDeadline" label="报名截止" width="170" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : row.status === 'ended' ? 'info' : 'warning'" size="small">
            {{ ({ active: '进行中', pending: '未开始', ended: '已结束' } as Record<string, string>)[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row)">详情</el-button>
          <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" text type="primary" @click="handleViewRegistrations(row)">报名名单</el-button>
          <el-button size="small" text type="warning" @click="handleExportResults(row)">导出成绩</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 创建/编辑赛事弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑赛事' : '创建赛事'" width="600px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="赛事名称" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入赛事名称" />
        </el-form-item>
        <el-form-item label="赛事简介" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入赛事简介" />
        </el-form-item>
        <el-form-item label="参赛对象" prop="targetGroup">
          <el-input v-model="createForm.targetGroup" placeholder="例如：小学组、初中组" />
        </el-form-item>
        <el-form-item label="赛程时间" prop="schedule">
          <el-input v-model="createForm.schedule" placeholder="例如：初赛3月1日，决赛3月15日" />
        </el-form-item>
        <el-form-item label="奖项设置" prop="awards">
          <el-input v-model="createForm.awards" type="textarea" :rows="2" placeholder="请输入奖项设置" />
        </el-form-item>
        <el-form-item label="报名截止" prop="registerDeadline">
          <el-date-picker v-model="createForm.registerDeadline" type="datetime" placeholder="选择报名截止时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="createForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="createForm.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="createForm.duration" :min="30" :max="300" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="createForm.totalScore" :min="1" :max="500" style="width: 100%" />
        </el-form-item>
        <el-form-item label="比赛须知" prop="notice">
          <el-input v-model="createForm.notice" type="textarea" :rows="4" placeholder="请输入比赛须知、规则、注意事项等公告内容" />
        </el-form-item>
        <el-form-item label="报名须知" prop="registerNotice">
          <el-input v-model="createForm.registerNotice" type="textarea" :rows="3" placeholder="请输入报名须知、参赛要求等" />
        </el-form-item>
        <el-form-item label="准考证信息">
          <el-row :gutter="16">
            <el-col :span="24">
              <el-input v-model="createForm.examTime" placeholder="考试时间，如：2026年7月15日 09:00-11:00" />
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 8px;">
            <el-col :span="24">
              <el-input v-model="createForm.examLocation" placeholder="请输入考试地点" />
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 8px;">
            <el-col :span="24">
              <el-input-number v-model="createForm.examDuration" :min="30" :max="300" :step="30" placeholder="考试时长(分钟)" style="width: 100%" />
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 报名名单弹窗 -->
    <el-dialog v-model="registrationDialogVisible" :title="`报名名单 - ${currentExam?.title || ''}`" width="500px">
      <div v-if="currentExam">
        <p>当前参赛人数：<strong>{{ currentExam.participantCount ?? 0 }}</strong> 人</p>
        <el-empty v-if="!currentExam.participantCount" description="暂无报名数据" />
        <p v-else style="color: #86909C; margin-top: 12px;">详细的报名名单数据将在后续版本中提供。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="registrationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 赛事详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="赛事详情" width="560px">
      <div v-if="viewRow" class="view-body" style="line-height: 2;">
        <p><strong>赛事ID：</strong>{{ viewRow.id }}</p>
        <p><strong>赛事名称：</strong>{{ viewRow.title }}</p>
        <p><strong>考试时长：</strong>{{ viewRow.duration }} 分钟</p>
        <p><strong>总分：</strong>{{ viewRow.totalScore }}</p>
        <p><strong>题目数量：</strong>{{ viewRow.questionCount }}</p>
        <p><strong>参赛人数：</strong>{{ viewRow.participantCount }}</p>
        <p><strong>报名截止：</strong>{{ viewRow.registerDeadline }}</p>
        <p><strong>开始时间：</strong>{{ viewRow.startTime }}</p>
        <p><strong>结束时间：</strong>{{ viewRow.endTime }}</p>
        <p><strong>当前状态：</strong>{{ viewRow.status === 'active' ? '进行中' : viewRow.status === 'ended' ? '已结束' : '未开始' }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamManagePage - 赛事管理后台
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { examApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('exam')

const pageNum = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const statusFilter = ref('')
const selectedIds = ref<number[]>([])

const examList = ref<any[]>([])

// 按搜索条件筛选后的列表
const filteredList = computed(() => {
  return examList.value.filter((item: any) => {
    const matchKeyword = !keyword.value || item.title?.toLowerCase().includes(keyword.value.toLowerCase())
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
  const total = examList.value.length
  const active = examList.value.filter(e => e.status === 'active').length
  const pending = examList.value.filter(e => e.status === 'pending').length
  const ended = examList.value.filter(e => e.status === 'ended').length
  return { total, active, pending, ended }
})

const viewDialogVisible = ref(false)
const viewRow = ref<any>(null)

function handleView(row: any) {
  viewRow.value = row
  viewDialogVisible.value = true
}

function handleExport() {
  const headers = ['ID', '赛事名称', '时长(分)', '总分', '题目数', '参赛人数', '报名截止', '开始时间', '结束时间', '状态']
  const rows = filteredList.value.map(e => [e.id, e.title, e.duration, e.totalScore, e.questionCount, e.participantCount, e.registerDeadline, e.startTime, e.endTime, e.status === 'active' ? '进行中' : e.status === 'ended' ? '已结束' : '未开始'])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `赛事数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出赛事数据')
}

/** 拉取赛事列表 */
async function handleSearch() {
  try {
    const res = await examApi.list({
      keyword: keyword.value,
      status: statusFilter.value,
      pageNum: 1,
      pageSize: 9999,
    })
    examList.value = res?.data?.list || res?.data?.records || []
  } catch {
    examList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  }
}
function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}
function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 项吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await examApi.batchRemove(selectedIds.value)
        examList.value = examList.value.filter(e => !selectedIds.value.includes(e.id))
        selectedIds.value = []
        ElMessage.success('批量删除成功')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}
function handleBatchPublish() {
  ElMessageBox.confirm(`确定批量发布选中的 ${selectedIds.value.length} 项吗？`, '提示', { type: 'warning' })
    .then(async () => {
      let failed = 0
      const succeeded: number[] = []
      for (const id of selectedIds.value) {
        try {
          await examApi.update(id, { status: 1 })
          succeeded.push(id)
        } catch {
          failed++
        }
      }
      examList.value.forEach(e => {
        if (succeeded.includes(e.id)) e.status = 'active'
      })
      selectedIds.value = []
      if (failed > 0) {
        ElMessage.error(`${failed} 项发布失败，请检查网络或稍后重试`)
      } else {
        ElMessage.success('批量发布成功')
      }
    })
    .catch(() => {})
}
function handleEdit(row: any) {
  isEdit.value = true
  editingId.value = row.id
  createForm.value = {
    title: row.title || '',
    description: row.description || '',
    targetGroup: row.targetGroup || '',
    schedule: row.schedule || '',
    awards: row.awards || '',
    registerDeadline: row.registerDeadline || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    duration: row.duration ?? 90,
    totalScore: row.totalScore ?? 100,
    notice: row.notice || '',
    registerNotice: row.registerNotice || '',
    examTime: row.examTime || '',
    examLocation: row.examLocation || '',
    examDuration: row.examDuration ?? row.duration ?? 120,
  }
  dialogVisible.value = true
}
function handleViewRegistrations(row: any) {
  currentExam.value = row
  registrationDialogVisible.value = true
}
function handleExportResults(row: any) { ElMessage.info(`导出成绩: ${row.title}`) }
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定删除赛事 "${row.title}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await examApi.remove(row.id)
        const index = examList.value.findIndex(e => e.id === row.id)
        if (index > -1) {
          examList.value.splice(index, 1)
          ElMessage.success('删除成功')
          opLog.logDelete('删除赛事「' + row.title + '」')
        }
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

// 创建/编辑赛事弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const createFormRef = ref<FormInstance>()

// 报名名单弹窗
const registrationDialogVisible = ref(false)
const currentExam = ref<any>(null)
const createForm = ref({
  title: '',
  description: '',
  targetGroup: '',
  schedule: '',
  awards: '',
  registerDeadline: '',
  startTime: '',
  endTime: '',
  duration: 90,
  totalScore: 100,
  notice: '',
  registerNotice: '',
  examTime: '',
  examLocation: '',
  examDuration: 120,
})
const createRules: FormRules = {
  title: [{ required: true, message: '请输入赛事名称', trigger: 'blur' }],
  registerDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'change' }],
}

function handleCreate() {
  isEdit.value = false
  editingId.value = null
  createForm.value = { title: '', description: '', targetGroup: '', schedule: '', awards: '', registerDeadline: '', startTime: '', endTime: '', duration: 90, totalScore: 100, notice: '', registerNotice: '', examTime: '', examLocation: '', examDuration: 120 }
  dialogVisible.value = true
}

async function submitForm() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = { ...createForm.value }
      if (isEdit.value && editingId.value != null) {
        await examApi.update(editingId.value, payload)
        const idx = examList.value.findIndex(e => e.id === editingId.value)
        if (idx > -1) {
          examList.value[idx] = { ...examList.value[idx], ...payload }
        }
        ElMessage.success('编辑赛事成功')
        opLog.logUpdate('编辑赛事「' + createForm.value.title + '」')
      } else {
        const res = await examApi.create(payload)
        const newId = res?.data?.id ?? Date.now()
        examList.value.push({
          id: newId,
          ...payload,
          questionCount: 0,
          participantCount: 0,
          status: 'pending',
        })
        ElMessage.success('创建赛事成功')
        opLog.logCreate('创建赛事「' + createForm.value.title + '」')
      }
      dialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.exam-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}
</style>
