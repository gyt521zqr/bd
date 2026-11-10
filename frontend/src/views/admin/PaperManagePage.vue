<template>
  <!-- 试卷中心 -->
  <div class="paper-manage-page">
    <div class="page-header">
      <h2>试卷中心</h2>
      <el-button type="primary" @click="openCreateDialog">创建试卷</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">试卷总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.active }}</div>
        <div class="stat-label">已启用</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #FF9500;">{{ stats.draft }}</div>
        <div class="stat-label">草稿</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #165DFF;">{{ stats.totalQuestions }}</div>
        <div class="stat-label">总题数</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="搜索试卷名称" clearable style="width: 240px" />
      <el-select v-model="searchForm.examId" placeholder="关联赛事" clearable style="width: 180px">
        <el-option v-for="exam in examOptions" :key="exam.value" :label="exam.label" :value="exam.value" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="启用" value="active" />
        <el-option label="禁用" value="disabled" />
        <el-option label="草稿" value="draft" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="pagedList" stripe @selection-change="handleSelectionChange" v-loading="loading" empty-text="暂无数据">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="试卷名称" min-width="200" />
      <el-table-column prop="examName" label="关联赛事" min-width="160" />
      <el-table-column prop="questionCount" label="题目数量" width="90" />
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column prop="duration" label="时长(分)" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" text type="primary" @click="handlePreview(row)">预览</el-button>
          <el-button size="small" text type="warning" @click="handleStatus(row)">{{ row.status === 'active' ? '禁用' : '启用' }}</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="filteredList.length" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 创建/编辑试卷弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑试卷' : '创建试卷'" width="900px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <h4 class="section-title">基本信息</h4>
        <el-form-item label="试卷名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="关联赛事" prop="examId">
          <el-select v-model="form.examId" placeholder="选择关联赛事" style="width: 100%">
            <el-option v-for="exam in examOptions" :key="exam.value" :label="exam.label" :value="exam.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" :min="30" :max="300" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="form.totalScore" :min="1" :max="500" style="width: 100%" />
        </el-form-item>

        <h4 class="section-title">组卷方式</h4>
        <el-form-item label="组卷方式" prop="composeType">
          <el-radio-group v-model="form.composeType">
            <el-radio-button label="select">从题库选择</el-radio-button>
            <el-radio-button label="import">批量导入</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 从题库选择 -->
        <template v-if="form.composeType === 'select'">
          <div class="compose-body">
            <div class="compose-left">
              <h4>题库列表</h4>
              <el-input v-model="composeSearch" placeholder="搜索题目" size="small" clearable />
              <el-select v-model="composeType" placeholder="题型筛选" size="small" clearable style="width: 100%; margin-top: 8px;">
                <el-option label="单选题" value="single" />
                <el-option label="多选题" value="multiple" />
                <el-option label="判断题" value="judge" />
                <el-option label="填空题" value="fill" />
                <el-option label="编程题" value="program" />
              </el-select>
              <div class="compose-list">
                <div
                  v-for="q in composeAvailableList"
                  :key="q.id"
                  class="compose-item"
                  @click="addToPaper(q)"
                >
                  <span class="compose-item-type">{{ typeMap[q.type] }}</span>
                  <span class="compose-item-content" :title="q.content">{{ q.content }}</span>
                  <el-button size="small" text type="primary">添加</el-button>
                </div>
              </div>
            </div>
            <div class="compose-right">
              <h4>已选题目（{{ form.questions.length }}）</h4>
              <div class="compose-list">
                <div
                  v-for="(q, idx) in form.questions"
                  :key="q.id"
                  class="compose-item selected"
                  draggable="true"
                  @dragstart="dragStart(idx)"
                  @dragover.prevent
                  @drop="drop(idx)"
                >
                  <span class="compose-item-index">{{ idx + 1 }}</span>
                  <span class="compose-item-content" :title="q.content">{{ q.content }}</span>
                  <el-input-number v-model="q.score" :min="1" :max="100" size="small" style="width: 90px" />
                  <el-button size="small" text type="danger" @click="removeFromPaper(idx)">移除</el-button>
                </div>
              </div>
              <div class="compose-actions">
                <el-button size="small" @click="batchSetScore">批量设置分值</el-button>
              </div>
            </div>
          </div>
        </template>

        <!-- 批量导入 -->
        <template v-if="form.composeType === 'import'">
          <el-upload drag action="#" :auto-upload="false" :on-change="handleImportFileChange" accept=".xlsx,.docx">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
          </el-upload>
          <div style="text-align: center; margin-top: 8px;">
            <el-button link type="primary" @click="downloadTemplate">下载导入模板</el-button>
          </div>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 试卷预览弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="试卷预览" width="800px" class="preview-dialog">
      <div v-if="previewPaper" class="preview-body">
        <div class="preview-header">
          <h3>{{ previewPaper.name }}</h3>
          <p>关联赛事：{{ previewPaper.examName }} | 时长：{{ previewPaper.duration }} 分钟 | 总分：{{ previewPaper.totalScore }} 分</p>
        </div>
        <div class="preview-questions">
          <div v-for="(q, idx) in previewPaper.questions" :key="q.id" class="preview-question">
            <p class="question-title">{{ idx + 1 }}. 【{{ typeMap[q.type] }}】({{ q.score }}分) {{ q.content }}</p>
            <div v-if="q.type === 'single' || q.type === 'multiple'" class="question-options">
              <p v-for="(opt, oidx) in q.options" :key="oidx">{{ optionLabels[oidx] }}. {{ opt }}</p>
            </div>
            <div v-if="q.type === 'judge'" class="question-options">
              <p>A. 对</p>
              <p>B. 错</p>
            </div>
            <div v-if="q.type === 'fill'" class="question-answer">
              <p>答：____________</p>
            </div>
            <div v-if="q.type === 'program'" class="question-code">
              <pre>{{ q.templateCode || '// 请在此编写代码' }}</pre>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * PaperManagePage - 试卷中心
 */
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import Pagination from '@/components/common/Pagination.vue'
import { paperApi } from '@/api/admin'
import { getExamList } from '@/api/exam'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('paper')

const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const selectedIds = ref<number[]>([])

const statusMap: Record<string, string> = {
  active: '启用',
  disabled: '禁用',
  draft: '草稿',
}
const typeMap: Record<string, string> = {
  single: '单选题',
  multiple: '多选题',
  judge: '判断题',
  fill: '填空题',
  program: '编程题',
}
const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F']

// 关联赛事下拉由后端 /exam/list 驱动，不再内置虚构赛事名
const examOptions = ref<Array<{ value: number; label: string }>>([])

async function loadExamOptions() {
  try {
    const res: any = await getExamList({ pageNum: 1, pageSize: 9999 })
    const rows = res?.data?.list || res?.data?.records || []
    examOptions.value = (Array.isArray(rows) ? rows : []).map((e: any) => ({
      value: Number(e.id),
      label: e.title || e.name || `赛事 #${e.id}`,
    }))
  } catch {
    examOptions.value = []
    ElMessage.error('关联赛事加载失败，请检查网络或稍后重试')
  }
}

const searchForm = reactive({
  name: '',
  examId: null as number | null,
  status: '',
})

function handleSearch() { pageNum.value = 1 }
function resetSearch() {
  searchForm.name = ''
  searchForm.examId = null
  searchForm.status = ''
  pageNum.value = 1
}

function statusTagType(status: string) {
  const map: Record<string, any> = { active: 'success', disabled: 'info', draft: 'warning' }
  return map[status] || ''
}

const mockQuestions: any[] = []

const paperList = ref<any[]>([])

const filteredList = computed(() => {
  let list = paperList.value
  if (searchForm.name) {
    list = list.filter(p => p.name.includes(searchForm.name))
  }
  if (searchForm.examId) {
    list = list.filter(p => p.examId === searchForm.examId)
  }
  if (searchForm.status) {
    list = list.filter(p => p.status === searchForm.status)
  }
  return list
})

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const stats = computed(() => {
  const total = paperList.value.length
  const active = paperList.value.filter(p => p.status === 'active').length
  const draft = paperList.value.filter(p => p.status === 'draft').length
  const totalQuestions = paperList.value.reduce((sum, p) => sum + (p.questionCount || 0), 0)
  return { total, active, draft, totalQuestions }
})

function handleExport() {
  const headers = ['ID', '试卷名称', '关联赛事', '题目数量', '总分', '时长(分)', '状态']
  const rows = filteredList.value.map(p => [p.id, p.name, p.examName, p.questionCount, p.totalScore, p.duration, statusMap[p.status] || p.status])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `试卷数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出试卷数据')
}

/** 加载试卷列表 */
async function loadPapers() {
  loading.value = true
  try {
    const res = await paperApi.list({ pageNum: 1, pageSize: 9999 })
    paperList.value = res?.data?.list || res?.data?.records || []
  } catch {
    paperList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPapers()
  loadExamOptions()
})

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}
function handleBatchDelete() {
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 份试卷吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await paperApi.batchRemove(selectedIds.value)
        paperList.value = paperList.value.filter(p => !selectedIds.value.includes(p.id))
        selectedIds.value = []
        ElMessage.success('批量删除成功')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}
function handleEdit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    name: row.name,
    examId: row.examId,
    duration: row.duration,
    totalScore: row.totalScore,
    composeType: 'select',
    questions: row.questions ? row.questions.map((q: any) => ({ ...q })) : [],
  }
  formDialogVisible.value = true
}
function handlePreview(row: any) {
  previewPaper.value = row
  previewDialogVisible.value = true
}
async function handleStatus(row: any) {
  const newStatus = row.status === 'active' ? 'disabled' : 'active'
  try {
    await paperApi.update(row.id, { status: newStatus })
    row.status = newStatus
    ElMessage.success(`已${newStatus === 'active' ? '启用' : '禁用'}试卷`)
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定删除试卷 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await paperApi.remove(row.id)
        paperList.value = paperList.value.filter(p => p.id !== row.id)
        ElMessage.success('删除成功')
        opLog.logDelete('删除试卷')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

// 表单
const formDialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const submitting = ref(false)
const formRef = ref<FormInstance>()

function getDefaultForm() {
  return {
    name: '',
    examId: null as number | null,
    duration: 90,
    totalScore: 100,
    composeType: 'select' as string,
    questions: [] as any[],
  }
}

const form = ref(getDefaultForm())

const formRules: FormRules = {
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  examId: [{ required: true, message: '请选择关联赛事', trigger: 'change' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'change' }],
  totalScore: [{ required: true, message: '请输入总分', trigger: 'change' }],
  composeType: [{ required: true, message: '请选择组卷方式', trigger: 'change' }],
}

function openCreateDialog() {
  isEdit.value = false
  form.value = getDefaultForm()
  formDialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const data: any = { ...form.value }
      data.questionCount = data.questions.length
      data.examName = examOptions.value.find(e => e.value === data.examId)?.label || ''
      if (isEdit.value) {
        await paperApi.update(currentId.value, data)
        const idx = paperList.value.findIndex(p => p.id === currentId.value)
        if (idx !== -1) paperList.value[idx] = { ...paperList.value[idx], ...data }
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑试卷')
      } else {
        const res = await paperApi.create(data)
        const newId = res?.data?.id ?? paperList.value.length + 1
        paperList.value.unshift({ ...data, id: newId, status: 'draft' })
        ElMessage.success('创建成功')
        opLog.logCreate('创建试卷')
      }
      formDialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 组卷
const composeSearch = ref('')
const composeType = ref('')
let dragIndex = -1

const composeAvailableList = computed(() => {
  let list = mockQuestions.filter(q => !form.value.questions.some((s: any) => s.id === q.id))
  if (composeSearch.value) {
    list = list.filter(q => q.content.includes(composeSearch.value))
  }
  if (composeType.value) {
    list = list.filter(q => q.type === composeType.value)
  }
  return list
})

function addToPaper(q: any) {
  form.value.questions.push({ ...q, score: 5 })
}
function removeFromPaper(idx: number) {
  form.value.questions.splice(idx, 1)
}
function dragStart(idx: number) {
  dragIndex = idx
}
function drop(idx: number) {
  if (dragIndex === -1 || dragIndex === idx) return
  const item = form.value.questions.splice(dragIndex, 1)[0]
  form.value.questions.splice(idx, 0, item)
  dragIndex = -1
}
function batchSetScore() {
  ElMessageBox.prompt('请输入分值', '批量设置分值', { inputType: 'number' })
    .then(({ value }) => {
      const score = Number(value)
      form.value.questions.forEach((q: any) => (q.score = score))
      ElMessage.success('设置成功')
    })
    .catch(() => {})
}

function handleImportFileChange(file: any) {
  ElMessage.success(`已选择文件：${file.name}`)
}
function downloadTemplate() {
  ElMessage.success('模板下载中...')
}

// 预览
const previewDialogVisible = ref(false)
const previewPaper = ref<any>(null)
</script>

<style scoped lang="scss">
$primary: #165DFF;

.paper-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { font-size: 20px; color: #1D2129; }
}
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}
.section-title { font-size: 14px; color: $text-primary; margin: 16px 0 12px; padding-left: 8px; border-left: 3px solid $primary; }

.compose-body { display: flex; gap: 16px; height: 400px;
  .compose-left, .compose-right { flex: 1; display: flex; flex-direction: column; border: 1px solid #E5E7EB; border-radius: 6px; padding: 12px; overflow: hidden;
    h4 { margin-bottom: 8px; font-size: 14px; color: $text-primary; }
  }
  .compose-list { flex: 1; overflow-y: auto; margin-top: 8px;
    .compose-item { display: flex; align-items: center; gap: 8px; padding: 8px; border-bottom: 1px solid #f0f0f0; cursor: pointer;
      &:hover { background: #f5f7fa; }
      .compose-item-type { flex-shrink: 0; font-size: 12px; color: $primary; background: rgba($primary, 0.1); padding: 2px 6px; border-radius: 4px; }
      .compose-item-index { flex-shrink: 0; font-size: 12px; color: #666; width: 20px; }
      .compose-item-content { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
      &.selected { cursor: move; }
    }
  }
  .compose-actions { display: flex; justify-content: flex-end; margin-top: 12px; }
}

.preview-body {
  .preview-header { text-align: center; margin-bottom: 24px; border-bottom: 2px solid #E5E7EB; padding-bottom: 16px;
    h3 { font-size: 20px; color: #1D2129; margin-bottom: 8px; }
    p { color: #666; font-size: 14px; }
  }
  .preview-questions { padding: 0 12px;
    .preview-question { margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px dashed #E5E7EB;
      .question-title { font-size: 15px; color: $text-primary; margin-bottom: 12px; line-height: 1.6; }
      .question-options { padding-left: 24px; color: #555; line-height: 1.8;
        p { margin-bottom: 4px; }
      }
      .question-answer { padding-left: 24px; color: #555; }
      .question-code { padding-left: 24px;
        pre { background: #f5f7fa; padding: 12px; border-radius: 6px; font-family: monospace; font-size: 13px; overflow-x: auto; }
      }
    }
  }
}
</style>
